package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class ThemeNotFoundException extends RuntimeException {
    public ThemeNotFoundException(UUID id) {
        super("Theme not found: " + id);
    }
}
