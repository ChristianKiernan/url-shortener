package com.christiankiernan.urlshortener.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.christiankiernan.urlshortener.models.ShortenedUrl;

public interface ShortenedUrlRepository extends JpaRepository<ShortenedUrl, Long>{
    ShortenedUrl findByShortcode(String shortenedUrl);
}
