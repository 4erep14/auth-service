package org.example.auth.exception.handler;

import org.example.auth.dto.ValidationErrorField;
import org.example.auth.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrorField> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ValidationErrorField(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ValidationErrorResponse response = new ValidationErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation error",
                fieldErrors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

    }
}
