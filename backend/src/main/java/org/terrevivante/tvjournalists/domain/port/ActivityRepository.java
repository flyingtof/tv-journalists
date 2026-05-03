package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Activity;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de persistance des activités rattachées aux journalistes.
 */
public interface ActivityRepository {
    Activity save(Activity activity);
    Optional<Activity> findById(UUID id);
}
