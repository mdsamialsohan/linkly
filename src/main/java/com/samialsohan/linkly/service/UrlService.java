package com.samialsohan.linkly.service;

import com.samialsohan.linkly.dto.ClickMetadata;
import com.samialsohan.linkly.dto.ShortenRequest;
import com.samialsohan.linkly.dto.ShortenResponse;
import com.samialsohan.linkly.entity.Url;
import com.samialsohan.linkly.event.ClickEvent;
import com.samialsohan.linkly.exception.NotFoundException;
import com.samialsohan.linkly.repository.UrlRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final UrlCacheService cache;
    private final String baseUrl;
    private final ClickEventPublisher clickEventPublisher;

    public UrlService(
            UrlRepository urlRepository,
            Base62Encoder base62Encoder,
            UrlCacheService cache,
            @Value("${linkly.base-url:http://localhost:8080}") String baseUrl,
            ClickEventPublisher clickEventPublisher) {
        this.urlRepository = urlRepository;
        this.base62Encoder = base62Encoder;
        this.cache = cache;
        this.baseUrl = baseUrl;
        this.clickEventPublisher = clickEventPublisher;
    }

    @Transactional
    public ShortenResponse shorten(ShortenRequest request) {
        long nextId = urlRepository.nextCodeSequenceValue();
        String shortCode = base62Encoder.encode(nextId);

        Url url = Url.builder()
                .shortCode(shortCode)
                .longUrl(request.longUrl())
                .clickCount(0L)
                .build();

        Url saved = urlRepository.save(url);
        cache.store(saved.getShortCode(), saved.getLongUrl());

        return new ShortenResponse(
                saved.getShortCode(),
                baseUrl + "/" + saved.getShortCode(),
                saved.getLongUrl(),
                saved.getCreatedAt(),
                saved.getExpiresAt()
        );
    }

    public String resolve(String shortCode, ClickMetadata metadata) {

       String longUrl = switch (cache.lookup(shortCode)) {
            case CacheLookup.Hit(String url) -> url;
            case CacheLookup.NegativeHit() -> throw new NotFoundException(
                    "No URL found for code: " + shortCode);
            case CacheLookup.Miss() -> resolveFromDatabase(shortCode);
        };
       clickEventPublisher.publish(new ClickEvent(
               shortCode, Instant.now(), metadata.referrer(), metadata.userAgent()
       ));
       return longUrl;
    }

    private String resolveFromDatabase(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .map(url -> {
                    cache.store(shortCode, url.getLongUrl());
                    return url.getLongUrl();
                })
                .orElseThrow(() -> {
                    cache.storeNotFound(shortCode);
                    return new NotFoundException(
                            "No URL found for code: " + shortCode);
                });
    }
}