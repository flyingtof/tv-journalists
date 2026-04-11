package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.Activity;

import java.util.Optional;
import java.util.UUID;

public interface ActivityRepository {
    Activity save(Activity activity);
    Optional<Activity> findById(UUID id);
}
