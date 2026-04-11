package org.terrevivante.tvjournalists.api.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.terrevivante.tvjournalists.application.exception.ActivityNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ActivityNotOwnedByJournalistException;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().build();
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
}
