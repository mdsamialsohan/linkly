package com.samialsohan.linkly.dto.analytics;

public record TopLink(
        String shortCode,
        String longUrl,
        long totalClicks
) {}