package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class JournalistNotFoundException extends RuntimeException {
    public JournalistNotFoundException(UUID id) {
        super("Journalist not found: " + id);
    }
}
