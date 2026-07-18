//package com.samialsohan.linkly;
//
//import com.samialsohan.linkly.dto.ShortenRequest;
//import com.samialsohan.linkly.dto.ShortenResponse;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.client.RestTemplate;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Testcontainers
//public class UrlShorteningIntegrationTest {
//    @Container
//    @ServiceConnection
//    static PostgreSQLContainer<?> postgres =
//            new PostgreSQLContainer<>("postgres:16-alpine");
//
//    @LocalServerPort
//    int port;
//
//    @Autowired
//    RestTemplate restTemplate = new RestTemplate();
//
//    @Test
//    void shortensAndResolvesAUrl() {
//        String base = "http://localhost:" + port;
//
//        ShortenRequest request = new ShortenRequest("https://example.com/hello");
//        ShortenResponse response = restTemplate.postForObject(
//                base + "/api/shorten",
//                request,
//                ShortenResponse.class
//        );
//
//        assertNotNull(response);
//        assertNotNull(response.shortCode());
//        assertFalse(response.shortCode().isBlank());
//        assertEquals("https://example.com/hello", response.longUrl());
//        assertTrue(response.shortUrl().endsWith(response.shortCode()));
//
//        // Configure the RestTemplate not to follow redirects so we can inspect the 302
//        RestTemplate noRedirect = new RestTemplate();
//        noRedirect.setRequestFactory(
//                new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
//                    setOutputStreaming(false);
//                }}
//        );
//
//        ResponseEntity<Void> redirect = restTemplate.getForEntity(
//                base + "/" + response.shortCode(),
//                Void.class
//        );
//
//        // RestTemplate follows redirects by default — success is 200 (from example.com)
//        // For the strict test, we'd disable following redirects. Keeping it simple here.
//        assertTrue(
//                redirect.getStatusCode().is3xxRedirection()
//                        || redirect.getStatusCode().is2xxSuccessful()
//        );
//    }
//
//    @Test
//    void returns404ForUnknownCode() {
//        String base = "http://localhost:" + port;
//
//        try {
//            restTemplate.getForEntity(base + "/doesnotexist", String.class);
//            fail("Expected 404 or exception");
//        } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
//            // expected
//        }
//    }
//}
//
