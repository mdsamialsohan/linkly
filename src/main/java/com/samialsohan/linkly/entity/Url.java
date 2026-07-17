package com.samialsohan.linkly.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.sql.Update;

import java.time.Instant;

@Entity
@Table(name="url")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name= "short_code", nullable = false, unique = true, length = 16)
    private String shortCode;
    @Column(name = "long_url", nullable = false, length = 2048)
    private String longUrl;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "expires_at")
    private Instant expiresAt;
    @Column(name="click_count", nullable = false)
    private Long clickCount;

    @PrePersist
    void onCreate() {
        if(createdAt == null) {
            createdAt = Instant.now();
        }
        if(clickCount == null) {
            clickCount = 0L;
        }
    }
}
