package com.samialsohan.linkly.controller;


import com.samialsohan.linkly.dto.analytics.ClickStats;
import com.samialsohan.linkly.dto.analytics.HourlyBucket;
import com.samialsohan.linkly.dto.analytics.ReferrerCount;
import com.samialsohan.linkly.dto.analytics.TopLink;
import com.samialsohan.linkly.service.AnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{shortCode}")
    public ClickStats stats(@PathVariable String shortCode) {
        return analyticsService.statsFor(shortCode);
    }

    @GetMapping("/{shortCode}/hourly")
    public List<HourlyBucket> hourly(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "24") int hoursBack
    ) {
        return analyticsService.hourlyTraffic(shortCode, hoursBack);
    }

    @GetMapping("/{shortCode}/referrers")
    public List<ReferrerCount> referrers(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "24") int hoursBack,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return analyticsService.referrers(shortCode, hoursBack, limit);
    }

    @GetMapping("/top")
    public List<TopLink> topLinks(
            @RequestParam(defaultValue = "24") int hoursBack,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return analyticsService.topLinks(hoursBack, limit);
    }
}