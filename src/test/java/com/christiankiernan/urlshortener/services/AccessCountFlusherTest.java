package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessCountFlusherTest {

    @Mock
    private AccessCountBuffer buffer;

    @Mock
    private ShortenedUrlRepository repository;

    @InjectMocks
    private AccessCountFlusher flusher;

    @Test
    void flush_doesNothingWhenBufferIsEmpty() {
        when(buffer.drainAll()).thenReturn(Map.of());

        flusher.flush();

        verifyNoInteractions(repository);
    }

    @Test
    void flush_writesEachDeltaToRepository() {
        when(buffer.drainAll()).thenReturn(Map.of("abc123", 3L, "xyz456", 1L));

        flusher.flush();

        verify(repository).incrementAccessCountBy("abc123", 3L);
        verify(repository).incrementAccessCountBy("xyz456", 1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void flush_callsDrainOnEveryInvocation() {
        when(buffer.drainAll()).thenReturn(Map.of());

        flusher.flush();
        flusher.flush();

        verify(buffer, times(2)).drainAll();
    }
}
