package org.terrevivante.tvjournalists.domain.port;

import org.terrevivante.tvjournalists.domain.model.ApplicationUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationUserRepository {
    ApplicationUser save(ApplicationUser user);
    Optional<ApplicationUser> findById(UUID id);
    Optional<ApplicationUser> findByUsername(String username);
    List<ApplicationUser> findAll();
}
