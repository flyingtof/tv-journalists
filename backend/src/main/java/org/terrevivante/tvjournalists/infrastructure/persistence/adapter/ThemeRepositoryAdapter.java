package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataThemeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class ThemeRepositoryAdapter implements ThemeRepository {

    private final SpringDataThemeRepository repo;
    private final PersistenceJournalistMapper mapper;

    public ThemeRepositoryAdapter(SpringDataThemeRepository repo, PersistenceJournalistMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Theme> findAll() {
        return repo.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Theme> findById(UUID id) {
        return repo.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Theme> findByNameIgnoreCase(String name) {
        return repo.findByNameIgnoreCase(name).map(mapper::toDomain);
    }

    @Override
    public Theme save(Theme theme) {
        Optional<ThemeEntity> existingEntity = theme.id() == null
            ? Optional.empty()
            : repo.findById(theme.id());

        if (theme.id() != null && existingEntity.isEmpty()) {
            throw new IllegalArgumentException("Theme not found: " + theme.id());
        }

        ThemeEntity entity = existingEntity.orElseGet(ThemeEntity::new);
        entity.setName(theme.name());
        return mapper.toDomain(repo.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInUse(UUID id) {
        return repo.existsInActivityById(id);
    }
}
