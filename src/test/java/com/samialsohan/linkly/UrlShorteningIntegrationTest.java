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

    @LocalServerPort
    int port;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    StringRedisTemplate redisTemplate;

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
}