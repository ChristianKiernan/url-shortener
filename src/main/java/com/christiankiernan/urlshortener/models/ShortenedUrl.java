package com.christiankiernan.urlshortener.models;

import jakarta.persistence.*;

import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * This class is a JPA entity that stores information about a URL and its shortened form,
 * along with metadata such as creation and update timestamps and the number of times the
 * shortened URL has been accessed.
 */
@Entity
public class ShortenedUrl {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false, unique = true)
    private String shortCode;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private int accessCount;

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public int getAccessCount() {
        return accessCount;
    }

    public void incrementAccessCount() {
        accessCount++;
    }

}
