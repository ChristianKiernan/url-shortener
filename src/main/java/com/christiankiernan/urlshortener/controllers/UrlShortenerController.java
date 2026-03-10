package com.christiankiernan.urlshortener.controllers;

import com.christiankiernan.urlshortener.dto.CreateUrlRequest;
import com.christiankiernan.urlshortener.dto.ShortenedUrlResponse;
import com.christiankiernan.urlshortener.dto.UpdateUrlRequest;
import com.christiankiernan.urlshortener.services.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing shortened URLs.
 *
 * <p>All endpoints are rooted at {@code /shorten}. Request bodies are validated
 * on arrival; invalid payloads receive 400 responses. Missing short codes
 * receive a 404 response. Both error shapes are handled by
 * {@link com.christiankiernan.urlshortener.exceptions.GlobalExceptionHandler}.
 */
@Tag(name = "URL Management", description = "Create, retrieve, update, and delete shortened URLs")
@RestController
@RequestMapping("/api/v1/shorten")
public class UrlShortenerController {

    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    /**
     * Creates a new shortened URL.
     *
     * @param request the request body containing the original URL
     * @return the created shortened URL with its generated short code
     */
    @Operation(summary = "Create a shortened URL", description = "Generates a unique 6-character short code for the given URL")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenedUrlResponse create(@Valid @RequestBody CreateUrlRequest request) {
        return ShortenedUrlResponse.from(service.createShortUrl(request.url()));
    }

    /**
     * Retrieves a shortened URL by its short code and increments its access count.
     *
     * @param code the short code to look up
     * @return the matching shortened URL
     * @throws com.christiankiernan.urlshortener.exceptions.NotFoundException if the code does not exist
     */
    @Operation(summary = "Get a shortened URL", description = "Retrieves the entry for a short code and increments its access count")
    @GetMapping("/{code}")
    public ShortenedUrlResponse getByShortCode(@PathVariable String code) {
        return ShortenedUrlResponse.from(service.getByShortCode(code));
    }

    /**
     * Updates the original URL associated with a short code.
     *
     * @param code    the short code of the entry to update
     * @param request the request body containing the new URL
     * @return the updated shortened URL
     * @throws com.christiankiernan.urlshortener.exceptions.NotFoundException if the code does not exist
     */
    @Operation(summary = "Update a shortened URL", description = "Replaces the original URL associated with a short code")
    @PutMapping("/{code}")
    public ShortenedUrlResponse update(@PathVariable String code, @Valid @RequestBody UpdateUrlRequest request) {
        return ShortenedUrlResponse.from(service.updateShortUrl(code, request.url()));
    }

    /**
     * Deletes the shortened URL with the given short code.
     *
     * @param code the short code of the entry to delete
     * @throws com.christiankiernan.urlshortener.exceptions.NotFoundException if the code does not exist
     */
    @Operation(summary = "Delete a shortened URL")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.deleteShortUrl(code);
    }

    /**
     * Retrieves access statistics for a shortened URL without incrementing its access count.
     *
     * @param code the short code to look up
     * @return the shortened URL including its current access count
     * @throws com.christiankiernan.urlshortener.exceptions.NotFoundException if the code does not exist
     */
    @Operation(summary = "Get access statistics", description = "Retrieves the entry for a short code without incrementing its access count")
    @GetMapping("/{code}/stats")
    public ShortenedUrlResponse getStats(@PathVariable String code) {
        return ShortenedUrlResponse.from(service.getStats(code));
    }

}
