package com.samialsohan.linkly.event;
import java.time.Instant;
public record ClickEvent(
        String shortCode,
        Instant clickedAt,
        String referrer,
        String userAgent)
{}