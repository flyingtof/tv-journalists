package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UUID id) {
        super("User not found: " + id);
    }

    public UserNotFoundException(String username) {
        super("User not found: " + username);
    }
}
