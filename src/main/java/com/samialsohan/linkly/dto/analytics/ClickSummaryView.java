package com.samialsohan.linkly.dto.analytics;

import java.time.Instant;

public interface ClickSummaryView {
    long getTotalClicks();
    Instant getFirstClick();
    Instant getLastClick();
}