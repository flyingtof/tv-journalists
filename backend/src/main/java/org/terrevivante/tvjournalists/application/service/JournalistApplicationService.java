package org.terrevivante.tvjournalists.application.service;

import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistsUseCase;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.List;
import java.util.UUID;

public class JournalistApplicationService
    implements CreateJournalistUseCase, GetJournalistUseCase, SearchJournalistsUseCase {

    private final JournalistRepository journalistRepository;

    public JournalistApplicationService(JournalistRepository journalistRepository) {
        this.journalistRepository = journalistRepository;
    }

    @Override
    public Journalist create(CreateJournalistCommand command) {
        Journalist journalist = new Journalist(
            null,
            command.firstName(),
            command.lastName(),
            command.globalEmail(),
            command.globalPhone(),
            null,
            null,
            List.of()
        );
        return journalistRepository.save(journalist);
    }

    @Override
    public Journalist getById(UUID id) {
        return journalistRepository.findById(id)
            .orElseThrow(() -> new JournalistNotFoundException(id));
    }

    @Override
    public PageResult<Journalist> search(JournalistSearchCriteria criteria, PageRequest pageRequest) {
        return journalistRepository.search(criteria, pageRequest);
    }
}
