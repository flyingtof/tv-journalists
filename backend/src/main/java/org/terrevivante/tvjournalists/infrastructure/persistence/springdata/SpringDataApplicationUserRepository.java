package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ApplicationUserEntity;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataApplicationUserRepository extends JpaRepository<ApplicationUserEntity, UUID> {
    Optional<ApplicationUserEntity> findByUsername(String username);
}
