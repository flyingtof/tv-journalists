package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.InteractionLog;

public interface InteractionLogRepository {
    InteractionLog save(InteractionLog interactionLog);
}
