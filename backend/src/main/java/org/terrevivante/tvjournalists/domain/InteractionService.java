package org.terrevivante.tvjournalists.domain;

import java.util.List;
import java.util.UUID;

public interface InteractionService {
    InteractionLog logInteraction(UUID journalistId, InteractionLog log);
    List<InteractionLog> findByJournalistId(UUID journalistId);
}
