package com.christiankiernan.urlshortener.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String shortCode) {
        super("Short code not found: " + shortCode);
    }
}
