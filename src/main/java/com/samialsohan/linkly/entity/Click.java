package com.samialsohan.linkly.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "click")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Click {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, length = 16)
    private String shortCode;

    @Column(name = "clicked_at", nullable = false)
    private Instant clickedAt;

    @Column(name = "referrer", length = 2048)
    private String referrer;

    @Column(name = "user_agent", length = 512)
    private String userAgent;
}
