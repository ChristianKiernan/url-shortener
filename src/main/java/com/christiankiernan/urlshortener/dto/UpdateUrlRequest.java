package com.christiankiernan.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUrlRequest(@NotBlank String url) {
}
