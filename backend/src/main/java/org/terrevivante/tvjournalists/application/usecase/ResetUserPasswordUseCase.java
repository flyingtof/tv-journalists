package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.application.command.ResetUserPasswordCommand;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

public interface ResetUserPasswordUseCase {
    ApplicationUser resetPassword(ResetUserPasswordCommand command);
}
