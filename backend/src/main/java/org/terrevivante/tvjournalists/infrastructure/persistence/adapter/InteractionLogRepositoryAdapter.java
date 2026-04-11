package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataInteractionLogRepository;

import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class InteractionLogRepositoryAdapter implements InteractionLogRepository {

    private final SpringDataInteractionLogRepository repo;
    private final PersistenceJournalistMapper mapper;

    public InteractionLogRepositoryAdapter(SpringDataInteractionLogRepository repo,
                                           PersistenceJournalistMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public InteractionLog save(InteractionLog interactionLog) {
        var entity = mapper.toEntity(interactionLog);
        return mapper.toDomain(repo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InteractionLog> findByJournalistId(UUID journalistId) {
        return repo.findByJournalistIdOrderByDateDesc(journalistId)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
