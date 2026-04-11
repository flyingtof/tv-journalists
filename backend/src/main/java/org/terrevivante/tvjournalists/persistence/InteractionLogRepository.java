package org.terrevivante.tvjournalists.persistence;

import org.terrevivante.tvjournalists.domain.InteractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionLogRepository extends JpaRepository<InteractionLog, UUID> {
    List<InteractionLog> findByJournalistIdOrderByDateDesc(UUID journalistId);
}
