package com.christiankiernan.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record UpdateUrlRequest(@NotBlank @URL String url) {
}
