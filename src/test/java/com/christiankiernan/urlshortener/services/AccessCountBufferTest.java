package com.christiankiernan.urlshortener.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessCountBufferTest {

    private static final String CODE = "abc123";
    private static final String KEY = AccessCountBuffer.KEY_PREFIX + CODE;

    @Mock
    private RedisTemplate<String, Long> redisTemplate;

    @Mock
    private ValueOperations<String, Long> valueOps;

    private AccessCountBuffer buffer;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
        buffer = new AccessCountBuffer(redisTemplate);
    }

    @Test
    void increment_callsRedisIncrWithPrefixedKey() {
        buffer.increment(CODE);

        verify(valueOps).increment(KEY);
    }

    @Test
    void drainAll_returnsEmptyMapWhenNoKeys() {
        when(redisTemplate.keys(AccessCountBuffer.KEY_PREFIX + "*")).thenReturn(Set.of());

        Map<String, Long> result = buffer.drainAll();

        assertThat(result).isEmpty();
        verifyNoMoreInteractions(valueOps);
    }

    @Test
    void drainAll_returnsDeltaWithPrefixStripped() {
        when(redisTemplate.keys(AccessCountBuffer.KEY_PREFIX + "*")).thenReturn(Set.of(KEY));
        when(valueOps.getAndDelete(KEY)).thenReturn(5L);

        Map<String, Long> result = buffer.drainAll();

        assertThat(result).containsEntry(CODE, 5L);
    }

    @Test
    void drainAll_skipsKeyWhenGetAndDeleteReturnsNull() {
        when(redisTemplate.keys(AccessCountBuffer.KEY_PREFIX + "*")).thenReturn(Set.of(KEY));
        when(valueOps.getAndDelete(KEY)).thenReturn(null);

        Map<String, Long> result = buffer.drainAll();

        assertThat(result).isEmpty();
    }

    @Test
    void drainAll_skipsKeyWhenDeltaIsZero() {
        when(redisTemplate.keys(AccessCountBuffer.KEY_PREFIX + "*")).thenReturn(Set.of(KEY));
        when(valueOps.getAndDelete(KEY)).thenReturn(0L);

        Map<String, Long> result = buffer.drainAll();

        assertThat(result).isEmpty();
    }

    @Test
    void delete_removesKeyFromRedis() {
        buffer.delete(CODE);

        verify(redisTemplate).delete(KEY);
    }
}
