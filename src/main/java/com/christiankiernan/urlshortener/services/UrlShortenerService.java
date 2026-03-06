package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.exceptions.NotFoundException;
import com.christiankiernan.urlshortener.models.ShortenedUrl;
import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
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
    private final SecureRandom random = new SecureRandom();
    private final ShortenedUrlRepository shortenedUrlRepository;

    public UrlShortenerService(ShortenedUrlRepository shortenedUrlRepository) {
        this.shortenedUrlRepository = shortenedUrlRepository;
    }

    /**
     * Creates a new shortened URL for the given original URL.
     * Generates a unique 6-character alphanumeric short code, retrying if a collision occurs.
     *
     * @param url the original URL to shorten
     * @return the newly created {@link ShortenedUrl} entity
     */
    @Transactional
    public ShortenedUrl createShortUrl(String url) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (shortenedUrlRepository.existsByShortCode(shortCode));  // retry on collision

        ShortenedUrl entity = new ShortenedUrl();
        entity.setUrl(url);
        entity.setShortCode(shortCode);

        return shortenedUrlRepository.save(entity);
    }

    /**
     * Retrieves a shortened URL by its short code and increments its access count.
     *
     * @param shortCode the short code to look up
     * @return the matching {@link ShortenedUrl} entity
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Transactional
    public ShortenedUrl getByShortCode(String shortCode) {
        ShortenedUrl entity = findOrThrow(shortCode);
        entity.incrementAccessCount();
        return entity;
    }

    /**
     * Updates the original URL associated with the given short code.
     *
     * @param shortCode the short code of the entry to update
     * @param newUrl    the new URL to associate with the short code
     * @return the updated {@link ShortenedUrl} entity
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Transactional
    public ShortenedUrl updateShortUrl(String shortCode, String newUrl) {
        ShortenedUrl entity = findOrThrow(shortCode);
        entity.setUrl(newUrl);
        return shortenedUrlRepository.save(entity);
    }

    /**
     * Deletes the shortened URL with the given short code.
     *
     * @param shortCode the short code of the entry to delete
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Transactional
    public void deleteShortUrl(String shortCode) {
        ShortenedUrl entity = findOrThrow(shortCode);
        shortenedUrlRepository.delete(entity);
    }

    /**
     * Retrieves the statistics for a shortened URL by its short code.
     *
     * @param shortCode the short code to look up
     * @return the {@link ShortenedUrl} entity including current access count
     * @throws NotFoundException if no entry exists for the given short code
     */
    @Transactional(readOnly = true)
    public ShortenedUrl getStats(String shortCode) {
        return findOrThrow(shortCode);
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
