package org.example.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        List<ValidationErrorField> validationErrors
) {
}
