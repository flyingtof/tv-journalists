package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.domain.model.Journalist;

import java.util.UUID;

public interface GetJournalistUseCase {
    Journalist getById(UUID id);
}
