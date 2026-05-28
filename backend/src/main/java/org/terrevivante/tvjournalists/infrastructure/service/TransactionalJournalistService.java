package org.terrevivante.tvjournalists.infrastructure.service;

import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.UpdateJournalistCommand;
import org.terrevivante.tvjournalists.application.service.JournalistApplicationService;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.DeleteJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistsUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateJournalistUseCase;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.UUID;

/**
 * Infrastructure-layer wrapper that applies Spring transaction boundaries around the
 * pure-Java {@link JournalistApplicationService}, keeping Spring annotations out of
 * {@code application/*}.
 */
@Transactional
public class TransactionalJournalistService
    implements CreateJournalistUseCase, UpdateJournalistUseCase, DeleteJournalistUseCase,
               GetJournalistUseCase, SearchJournalistsUseCase {

    private final JournalistApplicationService delegate;

    public TransactionalJournalistService(JournalistApplicationService delegate) {
        this.delegate = delegate;
    }

    @Override
    public Journalist create(CreateJournalistCommand command) {
        return delegate.create(command);
    }

    @Override
    public Journalist update(UpdateJournalistCommand command) {
        return delegate.update(command);
    }

    @Override
    public void delete(UUID id) {
        delegate.delete(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Journalist getById(UUID id) {
        return delegate.getById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Journalist> search(JournalistSearchCriteria criteria, PageRequest pageRequest) {
        return delegate.search(criteria, pageRequest);
    }
}
