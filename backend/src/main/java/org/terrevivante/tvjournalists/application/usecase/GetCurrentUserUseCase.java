package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

public interface GetCurrentUserUseCase {
    ApplicationUser getCurrentUser(String username);
}
