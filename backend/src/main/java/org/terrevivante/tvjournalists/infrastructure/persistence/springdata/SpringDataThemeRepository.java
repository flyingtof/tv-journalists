package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.UUID;

public interface SpringDataThemeRepository extends JpaRepository<ThemeEntity, UUID> {
}
