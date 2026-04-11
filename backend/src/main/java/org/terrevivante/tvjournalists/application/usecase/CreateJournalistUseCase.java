package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.domain.model.Journalist;

public interface CreateJournalistUseCase {
    Journalist create(CreateJournalistCommand command);
}
