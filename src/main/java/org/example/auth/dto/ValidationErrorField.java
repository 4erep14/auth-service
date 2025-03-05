package org.example.auth.dto;

public record ValidationErrorField(
        String field,
        String message
) {
}
