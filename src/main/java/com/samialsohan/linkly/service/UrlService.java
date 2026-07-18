package com.samialsohan.linkly.service;

import com.samialsohan.linkly.dto.ShortenRequest;
import com.samialsohan.linkly.dto.ShortenResponse;
import com.samialsohan.linkly.entity.Url;
import com.samialsohan.linkly.exception.NotFoundException;
import com.samialsohan.linkly.repository.UrlRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final Base62Encoder base62Encoder;
    private final String baseUrl;

    public UrlService(
            UrlRepository urlRepository,
            Base62Encoder base62Encoder,
            @Value("${linkly.base-url:http://localhost:8080}") String baseUrl
    ){
        this.urlRepository = urlRepository;
        this.base62Encoder = base62Encoder;
        this.baseUrl = baseUrl;
    }
    @Transactional
    public ShortenResponse shorten(ShortenRequest request)
    {
        long nextId = urlRepository.nextCodeSequenceValue();
        String shortCode = base62Encoder.encode(nextId);

        Url url = Url.builder()
                .shortCode(shortCode)
                .longUrl(request.longUrl())
                .clickCount(0L)
                .build();

        Url saved = urlRepository.save(url);
        return new ShortenResponse(
                saved.getShortCode(),
                baseUrl + "/" + saved.getShortCode(),
                saved.getLongUrl(),
                saved.getCreatedAt(),
                saved.getExpiresAt()
        );

    }

    @Transactional(readOnly = true)
    public String resolve(String shortCode)
    {
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("No URL found for code " + shortCode));
        return url.getLongUrl();
    }
}
