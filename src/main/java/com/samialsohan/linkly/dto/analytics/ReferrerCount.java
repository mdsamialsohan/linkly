package com.samialsohan.linkly.dto.analytics;

public record ReferrerCount(
        String referrer,
        long clicks
) {}