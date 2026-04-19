package org.terrevivante.tvjournalists.api.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.terrevivante.tvjournalists.api.dto.ValidationErrorResponse;
import org.terrevivante.tvjournalists.application.exception.ActivityNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ActivityNotOwnedByJournalistException;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;

import java.util.Comparator;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        ValidationErrorResponse response = new ValidationErrorResponse(
            "Validation failed",
            exception.getConstraintViolations().stream()
                .map(violation -> new ValidationErrorResponse.FieldError(
                    extractFieldName(violation.getPropertyPath().toString()),
                    violation.getMessage()))
                .sorted(Comparator
                    .comparing(ValidationErrorResponse.FieldError::field)
                    .thenComparing(ValidationErrorResponse.FieldError::message))
                .toList()
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(JournalistNotFoundException.class)
    public ResponseEntity<Void> handleJournalistNotFound(JournalistNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ResponseEntity<Void> handleActivityNotFound(ActivityNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ActivityNotOwnedByJournalistException.class)
    public ResponseEntity<Void> handleActivityNotOwnedByJournalist(ActivityNotOwnedByJournalistException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    private static String extractFieldName(String propertyPath) {
        int lastSeparator = propertyPath.lastIndexOf('.');
        return lastSeparator >= 0 ? propertyPath.substring(lastSeparator + 1) : propertyPath;
    }
}
