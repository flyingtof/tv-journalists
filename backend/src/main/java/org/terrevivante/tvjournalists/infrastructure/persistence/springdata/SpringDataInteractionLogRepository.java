package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.InteractionLogEntity;

import java.util.List;
import java.util.UUID;

public interface SpringDataInteractionLogRepository extends JpaRepository<InteractionLogEntity, UUID> {

    List<InteractionLogEntity> findByJournalistIdOrderByDateDesc(UUID journalistId);
}
