package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataThemeRepository;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class ThemeRepositoryAdapter implements ThemeRepository {

    private final SpringDataThemeRepository repo;
    private final PersistenceJournalistMapper mapper;

    public ThemeRepositoryAdapter(SpringDataThemeRepository repo, PersistenceJournalistMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public List<Theme> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }
}
