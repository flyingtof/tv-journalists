package org.terrevivante.tvjournalists.application.service;

import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.JournalistActivityUpsertCommand;
import org.terrevivante.tvjournalists.application.command.UpdateJournalistCommand;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.exception.MediaNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ThemeNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.DeleteJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistsUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateJournalistUseCase;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.List;
import java.util.UUID;

public class JournalistApplicationService
    implements CreateJournalistUseCase, UpdateJournalistUseCase, DeleteJournalistUseCase,
               GetJournalistUseCase, SearchJournalistsUseCase {

    private final JournalistRepository journalistRepository;
    private final InteractionLogRepository interactionLogRepository;
    private final MediaRepository mediaRepository;
    private final ThemeRepository themeRepository;
    private final ApplicationValidator applicationValidator;

    public JournalistApplicationService(JournalistRepository journalistRepository,
                                        InteractionLogRepository interactionLogRepository,
                                        MediaRepository mediaRepository,
                                        ThemeRepository themeRepository,
                                        ApplicationValidator applicationValidator) {
        this.journalistRepository = journalistRepository;
        this.interactionLogRepository = interactionLogRepository;
        this.mediaRepository = mediaRepository;
        this.themeRepository = themeRepository;
        this.applicationValidator = applicationValidator;
    }

    @Override
    @Transactional
    public Journalist create(CreateJournalistCommand command) {
        applicationValidator.validate(command);
        Journalist journalist = new Journalist(
            null,
            command.firstName().trim(),
            command.lastName().trim(),
            blankToNull(command.globalEmail()),
            blankToNull(command.globalPhone()),
            null,
            null,
            resolveActivities(null, command.activities())
        );
        return journalistRepository.save(journalist);
    }

    @Override
    @Transactional
    public Journalist update(UpdateJournalistCommand command) {
        applicationValidator.validate(command);
        Journalist existing = getById(command.id());
        Journalist updated = new Journalist(
            existing.id(),
            command.firstName().trim(),
            command.lastName().trim(),
            blankToNull(command.globalEmail()),
            blankToNull(command.globalPhone()),
            existing.createdAt(),
            existing.updatedAt(),
            resolveActivities(existing.id(), command.activities())
        );
        return journalistRepository.save(updated);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        getById(id);
        interactionLogRepository.deleteByJournalistId(id);
        journalistRepository.deleteById(id);
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

    private List<Activity> resolveActivities(UUID journalistId, List<JournalistActivityUpsertCommand> activities) {
        if (activities == null || activities.isEmpty()) return List.of();
        return activities.stream()
            .map(cmd -> {
                Media media = mediaRepository.findById(cmd.mediaId())
                    .orElseThrow(() -> new MediaNotFoundException(cmd.mediaId()));
                List<Theme> themes = cmd.themeIds().stream()
                    .map(themeId -> themeRepository.findById(themeId)
                        .orElseThrow(() -> new ThemeNotFoundException(themeId)))
                    .toList();
                return new Activity(
                    cmd.id(),
                    journalistId,
                    media,
                    cmd.role().trim(),
                    blankToNull(cmd.specificEmail()),
                    blankToNull(cmd.specificPhone()),
                    themes
                );
            })
            .toList();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
