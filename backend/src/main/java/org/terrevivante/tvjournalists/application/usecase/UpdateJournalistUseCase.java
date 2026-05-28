package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.UpdateJournalistCommand;
import org.terrevivante.tvjournalists.domain.model.Journalist;

public interface UpdateJournalistUseCase {
    Journalist update(UpdateJournalistCommand command);
}
