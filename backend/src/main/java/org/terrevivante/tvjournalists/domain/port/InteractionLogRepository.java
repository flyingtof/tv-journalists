package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.InteractionLog;

import java.util.UUID;

/**
 * Port d'écriture de l'historique des interactions avec les journalistes.
 */
public interface InteractionLogRepository {
    InteractionLog save(InteractionLog interactionLog);
    void deleteByJournalistId(UUID journalistId);
}
