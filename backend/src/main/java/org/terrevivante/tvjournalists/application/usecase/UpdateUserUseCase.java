package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.UpdateUserCommand;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

public interface UpdateUserUseCase {
    ApplicationUser update(UpdateUserCommand command);
}
