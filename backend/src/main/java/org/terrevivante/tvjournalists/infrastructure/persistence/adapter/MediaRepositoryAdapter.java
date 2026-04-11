package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataMediaRepository;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class MediaRepositoryAdapter implements MediaRepository {

    private final SpringDataMediaRepository repo;
    private final PersistenceJournalistMapper mapper;

    public MediaRepositoryAdapter(SpringDataMediaRepository repo, PersistenceJournalistMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<Media> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }
}
