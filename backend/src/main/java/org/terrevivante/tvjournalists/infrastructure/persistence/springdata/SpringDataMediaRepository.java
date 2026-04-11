package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;

import java.util.UUID;

public interface SpringDataMediaRepository extends JpaRepository<MediaEntity, UUID> {
}
