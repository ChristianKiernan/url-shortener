package com.christiankiernan.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUrlRequest(@NotBlank String url) {
}
