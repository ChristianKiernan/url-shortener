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
    private final AccessCountBuffer accessCountBuffer;
    private final UrlLookupService urlLookupService;

    public UrlShortenerService(ShortenedUrlRepository shortenedUrlRepository,
                               MeterRegistry meterRegistry,
                               AccessCountBuffer accessCountBuffer,
                               UrlLookupService urlLookupService) {
        this.shortenedUrlRepository = shortenedUrlRepository;
        this.urlsCreatedCounter = Counter.builder("url.shortener.urls.created")
                .description("Total number of shortened URLs created")
                .register(meterRegistry);
        this.accessCountBuffer = accessCountBuffer;
        this.urlLookupService = urlLookupService;
    }

    /**
     * Creates a shortened URL by generating a unique short code for the given URL
     * and saving it to the repository.
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
     * Records an access for the given short code and returns the URL response.
     * The Redis buffer is incremented on every call — including cache hits — because
     * this method itself is not cached. The underlying lookup delegates to
     * {@link UrlLookupService#getByShortCode}, which is cached and called through
     * its own Spring proxy.
     *
     * <p>Access counts are flushed to PostgreSQL asynchronously by {@link AccessCountFlusher}.
     * Stats may lag by up to one flush interval.
     *
     * @param shortCode the short code to look up
     * @return the matching {@link ShortenedUrlResponse} DTO
     * @throws NotFoundException if no entry exists for the given short code
     */
    public ShortenedUrlResponse recordAccessAndGet(String shortCode) {
        accessCountBuffer.increment(shortCode);
        return urlLookupService.getByShortCode(shortCode);
    }

    /**
     * Updates the original URL associated with the given short code.
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
     * Also removes any buffered Redis counter to prevent phantom flushes.
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
        accessCountBuffer.delete(shortCode);
    }

    /**
     * Retrieves the statistics for a shortened URL by its short code.
     *
     * <p>Results are cached in the {@code stats} cache for 60 seconds.
     * Due to buffered counting, {@code accessCount} may lag by up to one flush interval.
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
