package com.samialsohan.linkly.controller;

import com.samialsohan.linkly.dto.ClickMetadata;
import com.samialsohan.linkly.dto.ShortenRequest;
import com.samialsohan.linkly.dto.ShortenResponse;
import com.samialsohan.linkly.dto.UrlSummaryResponse;
import com.samialsohan.linkly.service.UrlService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class UrlController {
    private final UrlService urlService;

    public UrlController(UrlService urlService)
    {
        this.urlService = urlService;
    }

    @PostMapping("/api/shorten")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenResponse shorten(@Valid @RequestBody ShortenRequest request)
    {
        return urlService.shorten(request);
    }

    @GetMapping("/api/urls")
    public List<UrlSummaryResponse> listAll()
    {
        return urlService.listAll();
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            @RequestHeader(value = "Referer", required = false) String referrer,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    )
    {
        String longUrl = urlService.resolve(shortCode, new ClickMetadata(referrer, userAgent));
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(longUrl))
                .build();
    }
}
