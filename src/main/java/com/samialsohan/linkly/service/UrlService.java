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
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 7;
    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private final UrlRepository urlRepository;
    private final SecureRandom random = new SecureRandom();
    private final String baseUrl;

    public UrlService(
            UrlRepository urlRepository,
            @Value("${linkly.base-url:http://localhost:8080}") String baseUrl
    ){
        this.urlRepository = urlRepository;
        this.baseUrl = baseUrl;
    }
    @Transactional
    public ShortenResponse shorten(ShortenRequest request)
    {
        String shortCode = generateUniqueShortCode();

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

    private String generateUniqueShortCode() {
        for(int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++){
            String candidate = randomCode();
            if(!urlRepository.existsByShortCode(candidate)){
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Failed to generate shortcode after" + MAX_GENERATION_ATTEMPTS + "attempts");
    }
    private String randomCode()
    {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for(int i = 0; i < CODE_LENGTH; i++)
        {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
