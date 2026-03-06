package com.christiankiernan.urlshortener.controllers;

import com.christiankiernan.urlshortener.dto.CreateUrlRequest;
import com.christiankiernan.urlshortener.dto.ShortenedUrlResponse;
import com.christiankiernan.urlshortener.dto.UpdateUrlRequest;
import com.christiankiernan.urlshortener.services.UrlShortenerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shorten")
public class UrlShortenerController {

    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShortenedUrlResponse create(@Valid @RequestBody CreateUrlRequest request) {
        return ShortenedUrlResponse.from(service.createShortUrl(request.url()));
    }

    @GetMapping("/{code}")
    public ShortenedUrlResponse getByShortCode(@PathVariable String code) {
        return ShortenedUrlResponse.from(service.getByShortCode(code));
    }

    @PutMapping("/{code}")
    public ShortenedUrlResponse update(@PathVariable String code, @Valid @RequestBody UpdateUrlRequest request) {
        return ShortenedUrlResponse.from(service.updateShortUrl(code, request.url()));
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.deleteShortUrl(code);
    }

    @GetMapping("/{code}/stats")
    public ShortenedUrlResponse getStats(@PathVariable String code) {
        return ShortenedUrlResponse.from(service.getStats(code));
    }

}
