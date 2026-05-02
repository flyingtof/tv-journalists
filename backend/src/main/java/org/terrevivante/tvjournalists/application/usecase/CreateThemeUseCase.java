package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.CreateThemeCommand;
import org.terrevivante.tvjournalists.domain.model.Theme;

public interface CreateThemeUseCase {
    Theme create(CreateThemeCommand command);
}
