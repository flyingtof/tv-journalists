package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;

public interface LogInteractionUseCase {
    InteractionLog log(LogInteractionCommand command);
}
