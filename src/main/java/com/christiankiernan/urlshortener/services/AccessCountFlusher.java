package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Periodically drains the {@link AccessCountBuffer} and writes accumulated
 * access count deltas to PostgreSQL. Counts are approximate; any buffered
 * counts lost on a crash will not be recovered.
 */
@Component
public class AccessCountFlusher {

    private final AccessCountBuffer buffer;
    private final ShortenedUrlRepository repository;

    public AccessCountFlusher(AccessCountBuffer buffer, ShortenedUrlRepository repository) {
        this.buffer = buffer;
        this.repository = repository;
    }

    /**
     * Drains all buffered counts and writes each as a delta UPDATE to the database.
     * Uses {@code fixedDelay} so a slow DB flush can't cause overlapping runs.
     */
    @Scheduled(fixedDelayString = "${app.access-count.flush-interval-ms:30000}")
    @Transactional
    public void flush() {
        Map<String, Long> deltas = buffer.drainAll();
        if (deltas.isEmpty()) {
            return;
        }
        deltas.forEach(repository::incrementAccessCountBy);
    }
}
