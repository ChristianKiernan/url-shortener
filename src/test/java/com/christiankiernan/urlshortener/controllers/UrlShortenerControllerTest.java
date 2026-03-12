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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UrlShortenerController.class, GlobalExceptionHandler.class})
class UrlShortenerControllerTest {

    private static final String TEST_URL = "https://example.com";
    private static final String TEST_CODE = "abc123";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UrlShortenerService service;

    @MockitoBean
    private CacheManager cacheManager;

    // POST /shorten

    @Test
    void create_returns201WithShortenedUrl() throws Exception {
        when(service.createShortUrl(TEST_URL)).thenReturn(buildShortenedUrl());

        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "https://example.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.url").value(TEST_URL))
                .andExpect(jsonPath("$.shortCode").value(TEST_CODE));
    }

    @Test
    void create_returns400WhenUrlIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": ""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns400WhenUrlIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "not-a-url"}
                                """))
                .andExpect(status().isBadRequest());
    }

    // GET /shorten/{code}

    @Test
    void getByShortCode_returns200WithShortenedUrl() throws Exception {
        when(service.getByShortCode(TEST_CODE)).thenReturn(buildShortenedUrlResponse());

        mockMvc.perform(get("/api/v1/shorten/{code}", TEST_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(TEST_URL))
                .andExpect(jsonPath("$.shortCode").value(TEST_CODE));
    }

    @Test
    void getByShortCode_returns404WhenCodeNotFound() throws Exception {
        when(service.getByShortCode(TEST_CODE)).thenThrow(new NotFoundException(TEST_CODE));

        mockMvc.perform(get("/api/v1/shorten/{code}", TEST_CODE))
                .andExpect(status().isNotFound());
    }

    // PUT /shorten/{code}

    @Test
    void update_returns200WithUpdatedUrl() throws Exception {
        ShortenedUrl updated = buildShortenedUrl();
        updated.setUrl("https://updated.com");
        when(service.updateShortUrl(eq(TEST_CODE), any())).thenReturn(updated);

        mockMvc.perform(put("/api/v1/shorten/{code}", TEST_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "https://updated.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://updated.com"));
    }

    @Test
    void update_returns400WhenUrlIsInvalid() throws Exception {
        mockMvc.perform(put("/api/v1/shorten/{code}", TEST_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "not-a-url"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_returns404WhenCodeNotFound() throws Exception {
        when(service.updateShortUrl(eq(TEST_CODE), any())).thenThrow(new NotFoundException(TEST_CODE));

        mockMvc.perform(put("/api/v1/shorten/{code}", TEST_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url": "https://updated.com"}
                                """))
                .andExpect(status().isNotFound());
    }

    // DELETE /shorten/{code}

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/shorten/{code}", TEST_CODE))
                .andExpect(status().isNoContent());

        verify(service).deleteShortUrl(TEST_CODE);
    }

    @Test
    void delete_returns404WhenCodeNotFound() throws Exception {
        doThrow(new NotFoundException(TEST_CODE)).when(service).deleteShortUrl(TEST_CODE);

        mockMvc.perform(delete("/api/v1/shorten/{code}", TEST_CODE))
                .andExpect(status().isNotFound());
    }

    // GET /shorten/{code}/stats

    @Test
    void getStats_returns200WithAccessCount() throws Exception {
        when(service.getStats(TEST_CODE)).thenReturn(buildShortenedUrlResponse());

        mockMvc.perform(get("/api/v1/shorten/{code}/stats", TEST_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessCount").value(0));
    }

    @Test
    void getStats_returns404WhenCodeNotFound() throws Exception {
        when(service.getStats(TEST_CODE)).thenThrow(new NotFoundException(TEST_CODE));

        mockMvc.perform(get("/api/v1/shorten/{code}/stats", TEST_CODE))
                .andExpect(status().isNotFound());
    }

    private ShortenedUrl buildShortenedUrl() {
        ShortenedUrl entity = new ShortenedUrl();
        entity.setUrl(TEST_URL);
        entity.setShortCode(TEST_CODE);
        return entity;
    }

    private ShortenedUrlResponse buildShortenedUrlResponse() {
        return ShortenedUrlResponse.from(buildShortenedUrl());
    }

}
