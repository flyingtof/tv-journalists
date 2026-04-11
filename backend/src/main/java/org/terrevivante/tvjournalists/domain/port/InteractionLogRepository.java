package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.InteractionLog;

import java.util.List;
import java.util.UUID;

public interface InteractionLogRepository {
    InteractionLog save(InteractionLog interactionLog);
    List<InteractionLog> findByJournalistId(UUID journalistId);
}
