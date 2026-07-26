package com.samialsohan.linkly.dto;

import java.time.Instant;

public record UrlSummaryResponse(
        String shortCode,
        String shortUrl,
        String longUrl,
        Instant createdAt,
        Instant expiresAt,
        Long clickCount
) {}
