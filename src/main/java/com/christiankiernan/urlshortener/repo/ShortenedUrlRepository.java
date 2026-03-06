package com.christiankiernan.urlshortener.repo;

import com.christiankiernan.urlshortener.models.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    ShortenedUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);
}
