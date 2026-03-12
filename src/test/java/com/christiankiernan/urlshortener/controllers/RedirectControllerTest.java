package com.christiankiernan.urlshortener.controllers;

import com.christiankiernan.urlshortener.dto.ShortenedUrlResponse;
import com.christiankiernan.urlshortener.exceptions.GlobalExceptionHandler;
import com.christiankiernan.urlshortener.exceptions.NotFoundException;
import com.christiankiernan.urlshortener.models.ShortenedUrl;
import com.christiankiernan.urlshortener.services.UrlShortenerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({RedirectController.class, GlobalExceptionHandler.class})
class RedirectControllerTest {

    private static final String TEST_URL = "https://example.com";
    private static final String TEST_CODE = "abc123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService service;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void redirect_returns302WithLocationHeader() throws Exception {
        ShortenedUrlResponse response = ShortenedUrlResponse.from(buildShortenedUrl());
        when(service.getByShortCode(TEST_CODE)).thenReturn(response);

        mockMvc.perform(get("/{code}", TEST_CODE))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", TEST_URL));
    }

    private ShortenedUrl buildShortenedUrl() {
        ShortenedUrl entity = new ShortenedUrl();
        entity.setUrl(TEST_URL);
        entity.setShortCode(TEST_CODE);
        return entity;
    }

    @Test
    void redirect_returns404WhenCodeNotFound() throws Exception {
        when(service.getByShortCode(TEST_CODE)).thenThrow(new NotFoundException(TEST_CODE));

        mockMvc.perform(get("/{code}", TEST_CODE))
                .andExpect(status().isNotFound());
    }
}
