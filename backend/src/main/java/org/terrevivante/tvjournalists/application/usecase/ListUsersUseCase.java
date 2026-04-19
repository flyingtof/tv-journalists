package org.terrevivante.tvjournalists.application.usecase;

import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

import java.util.List;

public interface ListUsersUseCase {
    List<ApplicationUser> list();
}
