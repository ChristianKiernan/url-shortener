package com.christiankiernan.urlshortener.dto;

import com.christiankiernan.urlshortener.models.ShortenedUrl;

import java.time.Instant;

public record ShortenedUrlResponse(
        Long id,
        String url,
        String shortCode,
        Instant createdAt,
        Instant updatedAt,
        int accessCount
) {
    public static ShortenedUrlResponse from(ShortenedUrl entity) {
        return new ShortenedUrlResponse(
                entity.getId(),
                entity.getUrl(),
                entity.getShortCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getAccessCount()
        );
    }
}
