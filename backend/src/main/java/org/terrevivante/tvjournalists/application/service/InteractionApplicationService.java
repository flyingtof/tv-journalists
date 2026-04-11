package org.terrevivante.tvjournalists.application.service;

import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.application.exception.ActivityNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ActivityNotOwnedByJournalistException;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.usecase.LogInteractionUseCase;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;

import java.time.OffsetDateTime;

public class InteractionApplicationService implements LogInteractionUseCase {

    private final InteractionLogRepository interactionLogRepository;
    private final JournalistRepository journalistRepository;
    private final ActivityRepository activityRepository;

    public InteractionApplicationService(InteractionLogRepository interactionLogRepository,
                                         JournalistRepository journalistRepository,
                                         ActivityRepository activityRepository) {
        this.interactionLogRepository = interactionLogRepository;
        this.journalistRepository = journalistRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public InteractionLog log(LogInteractionCommand command) {
        journalistRepository.findById(command.journalistId())
            .orElseThrow(() -> new JournalistNotFoundException(command.journalistId()));

        if (command.activityId() != null) {
            var activity = activityRepository.findById(command.activityId())
                .orElseThrow(() -> new ActivityNotFoundException(command.activityId()));
            if (!command.journalistId().equals(activity.journalistId())) {
                throw new ActivityNotOwnedByJournalistException(command.activityId(), command.journalistId());
            }
        }

        InteractionLog log = new InteractionLog(
            null,
            command.journalistId(),
            command.activityId(),
            command.date(),
            command.description(),
            command.createdBy(),
            OffsetDateTime.now()
        );
        return interactionLogRepository.save(log);
    }
}
