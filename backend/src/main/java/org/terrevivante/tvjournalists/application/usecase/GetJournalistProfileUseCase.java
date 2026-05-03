package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;

import java.util.UUID;

public interface GetJournalistProfileUseCase {
    JournalistProfileView getById(UUID id);
}

