package org.terrevivante.tvjournalists.api.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.terrevivante.tvjournalists.api.dto.ValidationErrorResponse;
import org.terrevivante.tvjournalists.application.exception.ActivityNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ActivityNotOwnedByJournalistException;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.exception.UserAlreadyExistsException;
import org.terrevivante.tvjournalists.application.exception.UserNotFoundException;

import java.util.Comparator;
import java.util.List;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest().body(validationErrorResponse(
            exception.getConstraintViolations().stream()
                .map(violation -> new ValidationErrorResponse.FieldError(
                    extractFieldName(violation.getPropertyPath().toString()),
                    violation.getMessage()))
                .toList()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(validationErrorResponse(
            exception.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList()
        ));
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Void> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Void> handleUserNotFound(UserNotFoundException exception) {
        return ResponseEntity.notFound().build();
    }

    private static String extractFieldName(String propertyPath) {
        int lastSeparator = propertyPath.lastIndexOf('.');
        return lastSeparator >= 0 ? propertyPath.substring(lastSeparator + 1) : propertyPath;
    }

    private ValidationErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return new ValidationErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ValidationErrorResponse validationErrorResponse(List<ValidationErrorResponse.FieldError> errors) {
        return new ValidationErrorResponse(
            "Validation failed",
            errors.stream()
                .sorted(Comparator
                    .comparing(ValidationErrorResponse.FieldError::field)
                    .thenComparing(ValidationErrorResponse.FieldError::message))
                .toList()
        );
    }
}
