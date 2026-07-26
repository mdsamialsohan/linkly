package com.samialsohan.linkly;

import com.samialsohan.linkly.dto.ShortenRequest;
import com.samialsohan.linkly.dto.ShortenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UrlShorteningIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static GenericContainer<?> redis =
            new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                    .withExposedPorts(6379);
    @Container
    @ServiceConnection
    static org.testcontainers.kafka.KafkaContainer kafka =
            new org.testcontainers.kafka.KafkaContainer(
                    DockerImageName.parse("apache/kafka:3.9.0"));

    @LocalServerPort
    int port;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    com.samialsohan.linkly.repository.UrlRepository urlRepository;

    @Autowired
    com.samialsohan.linkly.repository.ClickRepository clickRepository;

    @Autowired
    com.samialsohan.linkly.service.AnalyticsService analyticsService;

    private String base() {
        return "http://localhost:" + port;
    }

    private RestTemplate noRedirectTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod)
                    throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        return new RestTemplate(factory);
    }

    @Test
    void shortensAndRedirects() {
        ShortenResponse response = restTemplate.postForObject(
                base() + "/api/shorten",
                new ShortenRequest("https://example.com/hello"),
                ShortenResponse.class
        );

        assertNotNull(response);
        assertFalse(response.shortCode().isBlank());
        assertEquals("https://example.com/hello", response.longUrl());

        ResponseEntity<Void> redirect = noRedirectTemplate().getForEntity(
                base() + "/" + response.shortCode(),
                Void.class
        );

        assertEquals(HttpStatus.FOUND, redirect.getStatusCode());
        assertEquals("https://example.com/hello",
                redirect.getHeaders().getLocation().toString());
    }

    @Test
    void returns404ForUnknownCode() {
        assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.getForEntity(base() + "/doesnotexist", String.class)
        );
    }

    @Test
    void cachesMappingAfterShorten() {
        ShortenResponse response = restTemplate.postForObject(
                base() + "/api/shorten",
                new ShortenRequest("https://example.com/cached"),
                ShortenResponse.class
        );

        String cached = redisTemplate.opsForValue()
                .get("url:" + response.shortCode());

        assertEquals("https://example.com/cached", cached);
    }

    @Test
    void negativeCachesUnknownCodes() {
        assertThrows(
                HttpClientErrorException.NotFound.class,
                () -> restTemplate.getForEntity(base() + "/nosuchcode", String.class)
        );

        String cached = redisTemplate.opsForValue().get("url:nosuchcode");

        assertEquals("NOT_FOUND", cached);
    }

    @Test
    void recordsClickAsynchronously() {
        ShortenResponse response = restTemplate.postForObject(
                base() + "/api/shorten",
                new ShortenRequest("https://example.com/tracked"),
                ShortenResponse.class
        );

        ResponseEntity<Void> redirect = noRedirectTemplate().getForEntity(
                base() + "/" + response.shortCode(), Void.class);
        assertEquals(HttpStatus.FOUND, redirect.getStatusCode());

        org.awaitility.Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() -> {
                    assertEquals(1, clickRepository.countByShortCode(response.shortCode()));
                    long count = urlRepository.findByShortCode(response.shortCode())
                            .orElseThrow().getClickCount();
                    assertEquals(1L, count);
                });
    }



    @Test
    void aggregatesClicksIntoStats() {
        ShortenResponse response = restTemplate.postForObject(
                base() + "/api/shorten",
                new ShortenRequest("https://example.com/analytics"),
                ShortenResponse.class
        );
        String code = response.shortCode();

        // fire 4 clicks
        for (int i = 0; i < 4; i++) {
            noRedirectTemplate().getForEntity(base() + "/" + code, Void.class);
        }

        // wait for the async consumer to catch up
        org.awaitility.Awaitility.await()
                .atMost(java.time.Duration.ofSeconds(10))
                .untilAsserted(() ->
                        assertEquals(4L, analyticsService.statsFor(code).totalClicks()));

        // rollup should agree with the summary
        var hourly = analyticsService.hourlyTraffic(code, 24);
        long rollupTotal = hourly.stream().mapToLong(
                com.samialsohan.linkly.dto.analytics.HourlyBucket::clicks).sum();
        assertEquals(4L, rollupTotal);
    }

    @Test
    void analyticsForUnknownCodeReturns404() {
        assertThrows(
                org.springframework.web.client.HttpClientErrorException.NotFound.class,
                () -> restTemplate.getForEntity(
                        base() + "/api/analytics/nope", String.class)
        );
    }
}