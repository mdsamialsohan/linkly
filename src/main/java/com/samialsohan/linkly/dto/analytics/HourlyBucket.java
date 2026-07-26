package com.samialsohan.linkly.dto.analytics;

import java.time.Instant;

public record HourlyBucket(
        Instant hour,
        long clicks
) {}