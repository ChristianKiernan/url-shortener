package com.christiankiernan.urlshortener.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Buffers URL access counts in Redis using atomic INCR operations.
 * Counts are drained periodically by {@link AccessCountFlusher} and written to PostgreSQL.
 */
@Component
public class AccessCountBuffer {

    static final String KEY_PREFIX = "access_count:";

    private final RedisTemplate<String, Long> redisTemplate;

    public AccessCountBuffer(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically increments the Redis counter for the given short code by 1.
     */
    public void increment(String shortCode) {
        redisTemplate.opsForValue().increment(KEY_PREFIX + shortCode);
    }

    /**
     * Drains all buffered counts. For each key matching {@code access_count:*},
     * atomically retrieves and deletes its value using GETDEL.
     * Returns a map of shortCode to delta. Keys with null results are skipped.
     */
    public Map<String, Long> drainAll() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return Map.of();
        }

        Map<String, Long> result = new HashMap<>();
        for (String key : keys) {
            Long delta = redisTemplate.opsForValue().getAndDelete(key);
            if (delta != null && delta > 0) {
                result.put(key.substring(KEY_PREFIX.length()), delta);
            }
        }
        return result;
    }

    /**
     * Deletes the Redis counter for the given short code.
     * Called when a short URL is deleted to prevent phantom flushes.
     */
    public void delete(String shortCode) {
        redisTemplate.delete(KEY_PREFIX + shortCode);
    }
}
