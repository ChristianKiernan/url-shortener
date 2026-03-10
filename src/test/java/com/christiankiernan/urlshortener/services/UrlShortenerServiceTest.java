package com.christiankiernan.urlshortener.services;

import com.christiankiernan.urlshortener.exceptions.NotFoundException;
import com.christiankiernan.urlshortener.models.ShortenedUrl;
import com.christiankiernan.urlshortener.repo.ShortenedUrlRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    private static final String TEST_URL = "https://example.com";
    private static final String TEST_CODE = "abc123";

    @Mock
    private ShortenedUrlRepository repository;

    @InjectMocks
    private UrlShortenerService service;

    @Captor
    private ArgumentCaptor<ShortenedUrl> captor;

    @Test
    void createShortUrl_persistsEntityWithSuppliedUrl() {
        when(repository.existsByShortCode(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createShortUrl(TEST_URL);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUrl()).isEqualTo(TEST_URL);
    }

    @Test
    void createShortUrl_generatesAlphanumericShortCode() {
        when(repository.existsByShortCode(any())).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createShortUrl(TEST_URL);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getShortCode()).matches("[A-Za-z0-9]+");
    }

    @Test
    void createShortUrl_retriesCodeGenerationOnCollision() {
        when(repository.existsByShortCode(any())).thenReturn(true, false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.createShortUrl(TEST_URL);

        verify(repository, times(2)).existsByShortCode(any());
        verify(repository, times(1)).save(any());
    }

    @Test
    void createShortUrl_throwsWhenMaxRetriesExceeded() {
        when(repository.existsByShortCode(any())).thenReturn(true);

        assertThatThrownBy(() -> service.createShortUrl(TEST_URL))
                .isInstanceOf(IllegalStateException.class);
    }


    @Test
    void getByShortCode_incrementsAccessCount() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(buildShortenedUrl());

        ShortenedUrl result = service.getByShortCode(TEST_CODE);

        assertThat(result.getAccessCount()).isEqualTo(1);
    }

    @Test
    void getByShortCode_throwsNotFoundForUnknownCode() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(null);

        assertThatThrownBy(() -> service.getByShortCode(TEST_CODE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateShortUrl_persistsNewUrl() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(buildShortenedUrl());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ShortenedUrl result = service.updateShortUrl(TEST_CODE, "https://updated.com");

        assertThat(result.getUrl()).isEqualTo("https://updated.com");
    }

    @Test
    void updateShortUrl_throwsNotFoundForUnknownCode() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(null);

        assertThatThrownBy(() -> service.updateShortUrl(TEST_CODE, "https://updated.com"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteShortUrl_deletesEntityFromRepository() {
        ShortenedUrl entity = buildShortenedUrl();
        when(repository.findByShortCode(TEST_CODE)).thenReturn(entity);

        service.deleteShortUrl(TEST_CODE);

        verify(repository).delete(entity);
    }

    @Test
    void deleteShortUrl_throwsNotFoundForUnknownCode() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(null);

        assertThatThrownBy(() -> service.deleteShortUrl(TEST_CODE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getStats_returnsEntityWithoutIncrementingAccessCount() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(buildShortenedUrl());

        ShortenedUrl result = service.getStats(TEST_CODE);

        assertThat(result.getAccessCount()).isEqualTo(0);
    }

    @Test
    void getStats_throwsNotFoundForUnknownCode() {
        when(repository.findByShortCode(TEST_CODE)).thenReturn(null);

        assertThatThrownBy(() -> service.getStats(TEST_CODE))
                .isInstanceOf(NotFoundException.class);
    }

    private ShortenedUrl buildShortenedUrl() {
        ShortenedUrl entity = new ShortenedUrl();
        entity.setUrl(TEST_URL);
        entity.setShortCode(TEST_CODE);
        return entity;
    }
}
