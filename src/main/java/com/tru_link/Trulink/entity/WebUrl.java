package com.tru_link.Trulink.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "web_url", indexes = {@Index(name = "idx_user_created", columnList = "user_id, created_at")})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebUrl {

    @Id
    private UUID linkId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "short_key", nullable = false, unique = true, updatable = false)
    private String shortKey;


    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public WebUrl(String originalUrl, String shortKey, UUID userId) {
        this.linkId = UuidCreator.getTimeOrderedEpoch();
        this.originalUrl = originalUrl;
        this.shortKey = shortKey;
        this.userId = userId;
    }

    @PrePersist
    private void prePersist(){
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }
}
