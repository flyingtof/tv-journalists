package org.terrevivante.tvjournalists.application.service;

import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.port.JournalistReadRepository;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistProfileUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistListUseCase;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.UUID;

public class JournalistReadApplicationService implements SearchJournalistListUseCase, GetJournalistProfileUseCase {

    private final JournalistReadRepository journalistReadRepository;

    public JournalistReadApplicationService(JournalistReadRepository journalistReadRepository) {
        this.journalistReadRepository = journalistReadRepository;
    }

    @Override
    public PageResult<JournalistListItemView> search(JournalistSearchCriteria criteria, PageRequest pageRequest) {
        return journalistReadRepository.search(criteria, pageRequest);
    }

    @Override
    public JournalistProfileView getById(UUID id) {
        return journalistReadRepository.findProfileById(id)
            .orElseThrow(() -> new JournalistNotFoundException(id));
    }
}

