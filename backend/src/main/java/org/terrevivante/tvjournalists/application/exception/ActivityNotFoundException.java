package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException(UUID id) {
        super("Activity not found: " + id);
    }
}
