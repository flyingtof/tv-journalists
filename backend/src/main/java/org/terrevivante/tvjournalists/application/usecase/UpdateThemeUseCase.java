package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.UpdateThemeCommand;
import org.terrevivante.tvjournalists.domain.model.Theme;

public interface UpdateThemeUseCase {
    Theme update(UpdateThemeCommand command);
}
