package com.christiankiernan.urlshortener.repo;

import com.christiankiernan.urlshortener.models.ShortenedUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long> {
    ShortenedUrl findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Modifying
    @Query("UPDATE ShortenedUrl s SET s.accessCount = s.accessCount + :delta WHERE s.shortCode = :shortCode")
    void incrementAccessCountBy(@Param("shortCode") String shortCode, @Param("delta") long delta);
}
