package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.CreateUserCommand;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

public interface CreateUserUseCase {
    ApplicationUser create(CreateUserCommand command);
}
