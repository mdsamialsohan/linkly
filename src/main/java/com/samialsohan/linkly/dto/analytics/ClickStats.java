package com.samialsohan.linkly.dto.analytics;

import java.time.Instant;

public record ClickStats(
        String shortCode,
        long totalClicks,
        Instant firstClick,
        Instant lastClick
) {}