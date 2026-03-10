package com.christiankiernan.urlshortener.controllers;

import com.christiankiernan.urlshortener.services.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Handles short-code redirects at the root path.
 *
 * <p>{@code GET /{code}} resolves the original URL and returns a
 * {@code 302 Found} response, incrementing the access count on each visit.
 */
@Tag(name = "Redirect", description = "Resolves short codes and redirects to the original URL")
@RestController
public class RedirectController {

    private final UrlShortenerService service;

    public RedirectController(UrlShortenerService service) {
        this.service = service;
    }

    /**
     * Redirects to the original URL associated with the given short code.
     *
     * @param code the short code to resolve
     * @return a 302 Found response with the {@code Location} header set to the original URL
     * @throws com.christiankiernan.urlshortener.exceptions.NotFoundException if the code does not exist
     */
    @Operation(summary = "Redirect to original URL", description = "Resolves a short code and redirects to the original URL, incrementing the access count")
    @ApiResponse(responseCode = "302", description = "Redirect to the original URL")
    @ApiResponse(responseCode = "404", description = "Short code not found")
    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = service.getByShortCode(code).getUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
