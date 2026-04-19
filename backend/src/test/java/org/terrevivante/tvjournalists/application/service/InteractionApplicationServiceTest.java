package org.terrevivante.tvjournalists.application.service;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.application.exception.ActivityNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ActivityNotOwnedByJournalistException;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class InteractionApplicationServiceTest {

    private final InteractionLogRepository interactionLogRepository = mock(InteractionLogRepository.class);
    private final JournalistRepository journalistRepository = mock(JournalistRepository.class);
    private final ActivityRepository activityRepository = mock(ActivityRepository.class);
    private final ApplicationValidator applicationValidator =
        new ApplicationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    private final InteractionApplicationService service =
        new InteractionApplicationService(
            interactionLogRepository,
            journalistRepository,
            activityRepository,
            applicationValidator
        );

    @Test
    void shouldLogInteractionForExistingJournalist() {
        UUID journalistId = UUID.randomUUID();
        Journalist journalist = new Journalist(journalistId, "Alice", "Green", null, null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(journalist));

        LogInteractionCommand command = new LogInteractionCommand(
            journalistId, null, LocalDate.now(), "Press conference attended", null);
        InteractionLog saved = new InteractionLog(UUID.randomUUID(), journalistId, null,
            LocalDate.now(), "Press conference attended", null, OffsetDateTime.now());
        when(interactionLogRepository.save(any())).thenReturn(saved);

        InteractionLog result = service.log(command);

        assertThat(result.journalistId()).isEqualTo(journalistId);
        assertThat(result.description()).isEqualTo("Press conference attended");
    }

    @Test
    void shouldRejectInvalidCommandBeforeRepositoryInteraction() {
        LogInteractionCommand command = new LogInteractionCommand(
            UUID.randomUUID(), null, LocalDate.now().plusDays(1), "Press conference attended", null);

        assertThatThrownBy(() -> service.log(command))
            .isInstanceOf(ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("date"));
        verifyNoInteractions(interactionLogRepository, journalistRepository, activityRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenLoggingForUnknownJournalist() {
        UUID journalistId = UUID.randomUUID();
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.empty());

        LogInteractionCommand command = new LogInteractionCommand(
            journalistId, null, LocalDate.now(), "Some event", null);

        assertThatThrownBy(() -> service.log(command))
            .isInstanceOf(JournalistNotFoundException.class);
        verifyNoInteractions(interactionLogRepository);
    }

    @Test
    void shouldThrowActivityNotFoundExceptionWhenActivityIdProvidedButUnknown() {
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Journalist journalist = new Journalist(journalistId, "Alice", "Green", null, null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(journalist));
        when(activityRepository.findById(activityId)).thenReturn(Optional.empty());

        LogInteractionCommand command = new LogInteractionCommand(
            journalistId, activityId, LocalDate.now(), "Interview at office", null);

        assertThatThrownBy(() -> service.log(command))
            .isInstanceOf(ActivityNotFoundException.class);
        verifyNoInteractions(interactionLogRepository);
    }

    @Test
    void shouldLogInteractionWhenActivityIdExistsAndJournalistExists() {
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Journalist journalist = new Journalist(journalistId, "Alice", "Green", null, null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        Media media = new Media(UUID.randomUUID(), "Le Monde", MediaType.PRESS, null);
        Activity activity = new Activity(activityId, journalistId, media, "Reporter", null, null, List.of());
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(journalist));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activity));

        InteractionLog saved = new InteractionLog(UUID.randomUUID(), journalistId, activityId,
            LocalDate.now(), "Interview at office", null, OffsetDateTime.now());
        when(interactionLogRepository.save(any())).thenReturn(saved);

        LogInteractionCommand command = new LogInteractionCommand(
            journalistId, activityId, LocalDate.now(), "Interview at office", null);

        InteractionLog result = service.log(command);

        assertThat(result.activityId()).isEqualTo(activityId);
    }

    @Test
    void shouldThrowWhenActivityBelongsToDifferentJournalist() {
        UUID journalistId = UUID.randomUUID();
        UUID otherJournalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        Journalist journalist = new Journalist(journalistId, "Alice", "Green", null, null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        Media media = new Media(UUID.randomUUID(), "Le Monde", MediaType.PRESS, null);
        Activity activityOwnedByOther = new Activity(activityId, otherJournalistId, media, "Reporter", null, null, List.of());
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(journalist));
        when(activityRepository.findById(activityId)).thenReturn(Optional.of(activityOwnedByOther));

        LogInteractionCommand command = new LogInteractionCommand(
            journalistId, activityId, LocalDate.now(), "Cross-journalist attempt", null);

        assertThatThrownBy(() -> service.log(command))
            .isInstanceOf(ActivityNotOwnedByJournalistException.class);
        verifyNoInteractions(interactionLogRepository);
    }
}
