package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class ThemeInUseException extends RuntimeException {
    public ThemeInUseException(UUID id) {
        super("Theme is still in use: " + id);
    }
}
