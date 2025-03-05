package org.example.auth.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        LocalDateTime timestamp,
        String error,
        String message
) {
}
