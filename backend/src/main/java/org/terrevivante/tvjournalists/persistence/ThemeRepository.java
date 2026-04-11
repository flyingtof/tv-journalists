package org.terrevivante.tvjournalists.persistence;

import org.terrevivante.tvjournalists.domain.Theme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, UUID> {
}

