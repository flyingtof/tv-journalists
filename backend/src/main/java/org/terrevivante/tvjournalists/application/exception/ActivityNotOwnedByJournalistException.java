package org.terrevivante.tvjournalists.application.exception;

import java.util.UUID;

public class ActivityNotOwnedByJournalistException extends RuntimeException {
    public ActivityNotOwnedByJournalistException(UUID activityId, UUID journalistId) {
        super("Activity " + activityId + " does not belong to journalist " + journalistId);
    }
}
