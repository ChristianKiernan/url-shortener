package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.dto.UrlResponse;
import com.christiankiernan.urlshortener.exceptions.NotFoundException;
import com.christiankiernan.urlshortener.models.ShortenedUrl;
import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles cached URL lookups. Extracted as a separate bean so that
 * {@link UrlShortenerService#recordAccessAndGet} can call through the Spring
 * proxy and have {@code @Cacheable} fire correctly (avoids self-invocation bypass).
 */
@Service
public class UrlLookupService {

    private final ShortenedUrlRepository repository;

    public UrlLookupService(ShortenedUrlRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns the shortened URL response for the given short code, caching the
     * result in the {@code urls} cache for one hour.
     *
     * @param shortCode the short code to look up
     * @return the matching {@link ShortenedUrlResponse}
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Cacheable(value = "urls", key = "#shortCode")
    @Transactional(readOnly = true)
    public UrlResponse getByShortCode(String shortCode) {
        ShortenedUrl entity = repository.findByShortCode(shortCode);
        if (entity == null) {
            throw new NotFoundException(shortCode);
        }
        return UrlResponse.from(entity);
    }
}
