package org.terrevivante.tvjournalists.application.exception;

public class ThemeAlreadyExistsException extends RuntimeException {
    public ThemeAlreadyExistsException(String name) {
        super("Theme already exists: " + name);
    }
}
