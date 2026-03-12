package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.dto.ShortenedUrlResponse;
import com.christiankiernan.urlshortener.exceptions.NotFoundException;
import com.christiankiernan.urlshortener.models.ShortenedUrl;
import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Service for creating, retrieving, updating, and deleting shortened URLs,
 * and for tracking access statistics.
 */
@Service
public class UrlShortenerService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SHORT_CODE_LENGTH = 6;
    private static final int MAX_RETRIES = 5;
    private final SecureRandom random = new SecureRandom();
    private final ShortenedUrlRepository shortenedUrlRepository;
    private final Counter urlsCreatedCounter;

    public UrlShortenerService(ShortenedUrlRepository shortenedUrlRepository, MeterRegistry meterRegistry) {
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.urlsCreatedCounter = Counter.builder("url.shortener.urls.created")
                .description("Total number of shortened URLs created")
                .register(meterRegistry);
    }

    /**
     * Creates a shortened URL by generating a unique short code for the given URL
     * and saving it to the repository.
     * <p>
     * Retries a maximum number of times in case of short code collisions. If a unique
     * short code cannot be generated within the configured retries, an {@link IllegalStateException}
     * is thrown.
     *
     * @param url the original URL to be shortened
     * @return a {@link ShortenedUrl} object containing the original URL and the generated short code
     * @throws IllegalStateException if a unique short code cannot be generated after the configured retries
     */
    public ShortenedUrl createShortUrl(String url) {
        for (int attempts = 0; attempts < MAX_RETRIES; attempts++) {
            String code = generateShortCode();
            if (!shortenedUrlRepository.existsByShortCode(code)) {
                ShortenedUrl entity = new ShortenedUrl();
                entity.setUrl(url);
                entity.setShortCode(code);
                ShortenedUrl saved = shortenedUrlRepository.save(entity);
                urlsCreatedCounter.increment();
                return saved;
            }
        }
        throw new IllegalStateException("Failed to generate unique short code after " + MAX_RETRIES + " attempts");
    }

    /**
     * Retrieves a shortened URL by its short code and increments its access count.
     *
     * <p>Results are cached in the {@code urls} cache for one hour. On a cache hit
     * the method body is skipped entirely, meaning the access count is <em>not</em>
     * incremented — this is an accepted trade-off of the simple caching approach.
     *
     * @param shortCode the short code to look up
     * @return the matching {@link ShortenedUrlResponse} DTO
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Cacheable(value = "urls", key = "#shortCode")
    @Transactional
    public ShortenedUrlResponse getByShortCode(String shortCode) {
        ShortenedUrl entity = findOrThrow(shortCode);
        entity.incrementAccessCount();
        return ShortenedUrlResponse.from(entity);
    }

    /**
     * Updates the original URL associated with the given short code.
     *
     * <p>Evicts the {@code urls} and {@code stats} cache entries for this short code
     * so the next read fetches fresh data from the database.
     *
     * @param shortCode the short code of the entry to update
     * @param newUrl    the new URL to associate with the short code
     * @return the updated {@link ShortenedUrl} entity
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Caching(evict = {
            @CacheEvict(value = "urls", key = "#shortCode"),
            @CacheEvict(value = "stats", key = "#shortCode")
    })
    @Transactional
    public ShortenedUrl updateShortUrl(String shortCode, String newUrl) {
        ShortenedUrl entity = findOrThrow(shortCode);
        entity.setUrl(newUrl);
        return shortenedUrlRepository.save(entity);
    }

    /**
     * Deletes the shortened URL with the given short code.
     *
     * <p>Evicts the {@code urls} and {@code stats} cache entries for this short code.
     *
     * @param shortCode the short code of the entry to delete
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Caching(evict = {
            @CacheEvict(value = "urls", key = "#shortCode"),
            @CacheEvict(value = "stats", key = "#shortCode")
    })
    @Transactional
    public void deleteShortUrl(String shortCode) {
        ShortenedUrl entity = findOrThrow(shortCode);
        shortenedUrlRepository.delete(entity);
    }

    /**
     * Retrieves the statistics for a shortened URL by its short code.
     *
     * <p>Results are cached in the {@code stats} cache for 60 seconds.
     *
     * @param shortCode the short code to look up
     * @return the {@link ShortenedUrlResponse} DTO including current access count
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Cacheable(value = "stats", key = "#shortCode")
    @Transactional(readOnly = true)
    public ShortenedUrlResponse getStats(String shortCode) {
        return ShortenedUrlResponse.from(findOrThrow(shortCode));
    }

    private ShortenedUrl findOrThrow(String shortCode) {
        ShortenedUrl entity = shortenedUrlRepository.findByShortCode(shortCode);
        if (entity == null) {
            throw new NotFoundException(shortCode);
        }
        return entity;
    }

    private String generateShortCode() {
        StringBuilder sb = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
