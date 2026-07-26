package com.samialsohan.linkly.service;


import com.samialsohan.linkly.dto.analytics.*;
import com.samialsohan.linkly.exception.NotFoundException;
import com.samialsohan.linkly.repository.ClickRepository;
import com.samialsohan.linkly.repository.UrlRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class AnalyticsService {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    public AnalyticsService(
            ClickRepository clickRepository,
            UrlRepository urlRepository
    ) {
        this.clickRepository = clickRepository;
        this.urlRepository = urlRepository;
    }

    @Transactional(readOnly = true)
    public ClickStats statsFor(String shortCode) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new NotFoundException("No URL found for code: " + shortCode);
        }
        ClickSummaryView view = clickRepository.summaryFor(shortCode);
        return new ClickStats(
                shortCode,
                view.getTotalClicks(),
                view.getFirstClick(),
                view.getLastClick()
        );
    }

    @Transactional(readOnly = true)
    public List<HourlyBucket> hourlyTraffic(String shortCode, int hoursBack) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new NotFoundException("No URL found for code: " + shortCode);
        }
        Instant since = Instant.now().minus(Duration.ofHours(hoursBack));
        return clickRepository.hourlyClicksFromRollup(shortCode, since);
    }

    @Transactional(readOnly = true)
    public List<ReferrerCount> referrers(String shortCode, int hoursBack, int limit) {
        if (!urlRepository.existsByShortCode(shortCode)) {
            throw new NotFoundException("No URL found for code: " + shortCode);
        }
        Instant since = Instant.now().minus(Duration.ofHours(hoursBack));
        return clickRepository.referrerBreakdown(shortCode, since, limit);
    }

    @Transactional(readOnly = true)
    public List<TopLink> topLinks(int hoursBack, int limit) {
        Instant since = Instant.now().minus(Duration.ofHours(hoursBack));
        return urlRepository.topLinks(since, limit);
    }
}