package com.samialsohan.linkly.repository;

import com.samialsohan.linkly.dto.analytics.TopLink;
import com.samialsohan.linkly.entity.Url;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    @Query(value = "SELECT nextval('url_code_seq')", nativeQuery = true)
    long nextCodeSequenceValue();
    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    int incrementClickCount(@Param("shortCode") String shortCode);

    @Query(value = """
            SELECT u.short_code   AS shortCode,
                   u.long_url      AS longUrl,
                   COUNT(c.id)     AS totalClicks
            FROM url u
            JOIN click c ON c.short_code = u.short_code
            WHERE c.clicked_at >= :since
            GROUP BY u.short_code, u.long_url
            ORDER BY totalClicks DESC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<TopLink> topLinks(
            @Param("since") Instant since,
            @Param("limit") int limit);
}
