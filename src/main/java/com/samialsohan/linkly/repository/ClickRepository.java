package com.samialsohan.linkly.repository;

import com.samialsohan.linkly.dto.analytics.ClickSummaryView;
import com.samialsohan.linkly.dto.analytics.HourlyBucket;
import com.samialsohan.linkly.dto.analytics.ReferrerCount;
import com.samialsohan.linkly.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

    long countByShortCode(String shortCode);
    @Query(value = """
            SELECT date_trunc('hour', clicked_at) AS hour,
                   COUNT(*)                        AS clicks
            FROM click
            WHERE short_code = :shortCode
              AND clicked_at >= :since
            GROUP BY date_trunc('hour', clicked_at)
            ORDER BY hour
            """,
            nativeQuery = true)
    List<HourlyBucket> hourlyClicks(
            @Param("shortCode") String shortCode,
            @Param("since") Instant since);

    @Query(value = """
            SELECT COALESCE(referrer, 'direct') AS referrer,
                   COUNT(*)                      AS clicks
            FROM click
            WHERE short_code = :shortCode
              AND clicked_at >= :since
            GROUP BY COALESCE(referrer, 'direct')
            ORDER BY clicks DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<ReferrerCount> referrerBreakdown(
            @Param("shortCode") String shortCode,
            @Param("since") Instant since,
            @Param("limit") int limit);
    @Query(value = """
            SELECT COUNT(*)          AS totalClicks,
                   MIN(clicked_at)   AS firstClick,
                   MAX(clicked_at)   AS lastClick
            FROM click
            WHERE short_code = :shortCode
            """,
            nativeQuery = true)
    ClickSummaryView summaryFor(@Param("shortCode") String shortCode);
    @Modifying
    @Query(value = """
            INSERT INTO click_hourly (short_code, bucket_hour, click_count)
            VALUES (:shortCode, date_trunc('hour', CAST(:clickedAt AS timestamptz)), 1)
            ON CONFLICT (short_code, bucket_hour)
            DO UPDATE SET click_count = click_hourly.click_count + 1
            """,
            nativeQuery = true)
    void incrementHourlyRollup(
            @Param("shortCode") String shortCode,
            @Param("clickedAt") Instant clickedAt);

    @Query(value = """
            SELECT bucket_hour AS hour,
                   click_count AS clicks
            FROM click_hourly
            WHERE short_code = :shortCode
              AND bucket_hour >= :since
            ORDER BY bucket_hour
            """,
            nativeQuery = true)
    List<HourlyBucket> hourlyClicksFromRollup(
            @Param("shortCode") String shortCode,
            @Param("since") Instant since);

}