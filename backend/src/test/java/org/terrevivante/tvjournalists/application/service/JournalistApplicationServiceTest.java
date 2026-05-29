package org.terrevivante.tvjournalists.application.service;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.JournalistActivityUpsertCommand;
import org.terrevivante.tvjournalists.application.command.UpdateJournalistCommand;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import org.terrevivante.tvjournalists.application.exception.MediaNotFoundException;
import org.terrevivante.tvjournalists.application.exception.ThemeNotFoundException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JournalistApplicationServiceTest {

    private static final UUID MEDIA_ID = UUID.randomUUID();
    private static final Media SOME_MEDIA = new Media(MEDIA_ID, "TV1", MediaType.TV, null);

    private final JournalistRepository journalistRepository = mock(JournalistRepository.class);
    private final InteractionLogRepository interactionLogRepository = mock(InteractionLogRepository.class);
    private final MediaRepository mediaRepository = mock(MediaRepository.class);
    private final ThemeRepository themeRepository = mock(ThemeRepository.class);
    private final ApplicationValidator applicationValidator =
        new ApplicationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    private final JournalistApplicationService service =
        new JournalistApplicationService(journalistRepository, interactionLogRepository,
            mediaRepository, themeRepository, applicationValidator);

    @Test
    void shouldSearchUsingCoreCriteriaAndCustomPageTypes() {
        Journalist journalist = new Journalist(
            UUID.randomUUID(),
            "Alice",
            "Green",
            "alice@example.com",
            null,
            OffsetDateTime.now(),
            OffsetDateTime.now(),
            List.of()
        );
        when(journalistRepository.search(
            new JournalistSearchCriteria("ali", List.of("Green Press"), List.of("Biodiversity")),
            new PageRequest(0, 20)
        )).thenReturn(new PageResult<>(List.of(journalist), 1, 0, 20));

        PageResult<Journalist> result = service.search(
            new JournalistSearchCriteria("ali", List.of("Green Press"), List.of("Biodiversity")),
            new PageRequest(0, 20)
        );

        assertThat(result.content()).hasSize(1);
    }

    @Test
    void shouldCreateJournalistAndReturnSavedEntity() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", "bob@example.com", "+33600000000", List.of());
        Journalist saved = new Journalist(UUID.randomUUID(), "Bob", "Brown", "bob@example.com", "+33600000000",
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        when(journalistRepository.save(any())).thenReturn(saved);

        Journalist result = service.create(command);

        assertThat(result.firstName()).isEqualTo("Bob");
        assertThat(result.lastName()).isEqualTo("Brown");
    }

    @Test
    void shouldCreateJournalistWithResolvedActivities() {
        CreateJournalistCommand command = new CreateJournalistCommand(
            "Bob", "Brown", "bob@example.com", null,
            List.of(new JournalistActivityUpsertCommand(null, MEDIA_ID, null, null, List.of()))
        );
        when(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.of(SOME_MEDIA));
        when(journalistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Journalist result = service.create(command);

        assertThat(result.activities()).hasSize(1);
        assertThat(result.activities().get(0).media()).isEqualTo(SOME_MEDIA);
    }

    @Test
    void shouldRejectInvalidCreateCommandBeforeRepositoryInteraction() {
        CreateJournalistCommand command = new CreateJournalistCommand("  ", "Brown", null, null, List.of());

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("firstName"));
        verifyNoInteractions(journalistRepository);
    }

    @Test
    void shouldGetJournalistByIdWhenFound() {
        UUID id = UUID.randomUUID();
        Journalist journalist = new Journalist(id, "Alice", "Green", "alice@example.com", null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        when(journalistRepository.findById(id)).thenReturn(Optional.of(journalist));

        Journalist result = service.getById(id);

        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    void shouldThrowJournalistNotFoundExceptionWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(journalistRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(id))
            .isInstanceOf(JournalistNotFoundException.class);
    }

    @Test
    void shouldDeleteJournalistAndLinkedInteractions() {
        UUID journalistId = UUID.randomUUID();
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(existingJournalist(journalistId)));

        service.delete(journalistId);

        verify(interactionLogRepository).deleteByJournalistId(journalistId);
        verify(journalistRepository).deleteById(journalistId);
    }

    @Test
    void shouldUpdateExistingJournalistWithResolvedActivities() {
        UUID journalistId = UUID.randomUUID();
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(existingJournalist(journalistId)));
        when(journalistRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.of(SOME_MEDIA));

        Journalist result = service.update(updateCommand(journalistId));

        assertThat(result.activities()).hasSize(1);
        verify(journalistRepository).save(any(Journalist.class));
    }

    @Test
    void update_throwsJournalistNotFoundExceptionWhenJournalistMissing() {
        UUID id = UUID.randomUUID();
        when(journalistRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(updateCommand(id)))
            .isInstanceOf(JournalistNotFoundException.class);
        verifyNoInteractions(mediaRepository);
    }

    @Test
    void delete_throwsJournalistNotFoundExceptionWhenJournalistMissing() {
        UUID id = UUID.randomUUID();
        when(journalistRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(JournalistNotFoundException.class);
        verifyNoInteractions(interactionLogRepository);
    }

    @Test
    void create_propagatesMediaNotFoundException() {
        UUID unknownMediaId = UUID.randomUUID();
        CreateJournalistCommand command = new CreateJournalistCommand(
            "Bob", "Brown", null, null,
            List.of(new JournalistActivityUpsertCommand(null, unknownMediaId, null, null, List.of()))
        );
        when(mediaRepository.findById(unknownMediaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(MediaNotFoundException.class);
        verifyNoInteractions(journalistRepository);
    }

    @Test
    void create_propagatesThemeNotFoundException() {
        UUID unknownThemeId = UUID.randomUUID();
        CreateJournalistCommand command = new CreateJournalistCommand(
            "Bob", "Brown", null, null,
            List.of(new JournalistActivityUpsertCommand(null, MEDIA_ID, null, null, List.of(unknownThemeId)))
        );
        when(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.of(SOME_MEDIA));
        when(themeRepository.findById(unknownThemeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(command))
            .isInstanceOf(ThemeNotFoundException.class);
        verifyNoInteractions(journalistRepository);
    }

    @Test
    void update_propagatesMediaNotFoundException() {
        UUID journalistId = UUID.randomUUID();
        UUID unknownMediaId = UUID.randomUUID();
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(existingJournalist(journalistId)));
        when(mediaRepository.findById(unknownMediaId)).thenReturn(Optional.empty());

        UpdateJournalistCommand command = new UpdateJournalistCommand(
            journalistId, "Alice", "Martin", null, null,
            List.of(new JournalistActivityUpsertCommand(null, unknownMediaId, null, null, List.of()))
        );

        assertThatThrownBy(() -> service.update(command))
            .isInstanceOf(MediaNotFoundException.class);
    }

    @Test
    void update_propagatesThemeNotFoundException() {
        UUID journalistId = UUID.randomUUID();
        UUID unknownThemeId = UUID.randomUUID();
        when(journalistRepository.findById(journalistId)).thenReturn(Optional.of(existingJournalist(journalistId)));
        when(mediaRepository.findById(MEDIA_ID)).thenReturn(Optional.of(SOME_MEDIA));
        when(themeRepository.findById(unknownThemeId)).thenReturn(Optional.empty());

        UpdateJournalistCommand command = new UpdateJournalistCommand(
            journalistId, "Alice", "Martin", null, null,
            List.of(new JournalistActivityUpsertCommand(null, MEDIA_ID, null, null, List.of(unknownThemeId)))
        );

        assertThatThrownBy(() -> service.update(command))
            .isInstanceOf(ThemeNotFoundException.class);
    }

    private static Journalist existingJournalist(UUID journalistId) {
        return new Journalist(journalistId, "Alice", "Martin", null, null,
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
    }

    private UpdateJournalistCommand updateCommand(UUID journalistId) {
        return new UpdateJournalistCommand(
            journalistId,
            "Alice",
            "Martin",
            "alice@example.com",
            null,
            List.of(new JournalistActivityUpsertCommand(null, MEDIA_ID, null, null, List.of()))
        );
    }
}
