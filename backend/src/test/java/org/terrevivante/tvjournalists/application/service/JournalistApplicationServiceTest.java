package org.terrevivante.tvjournalists.application.service;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JournalistApplicationServiceTest {

    private final JournalistRepository journalistRepository = mock(JournalistRepository.class);
    private final ApplicationValidator applicationValidator =
        new ApplicationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    private final JournalistApplicationService service =
        new JournalistApplicationService(journalistRepository, applicationValidator);

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
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", "bob@example.com", "+33600000000");
        Journalist saved = new Journalist(UUID.randomUUID(), "Bob", "Brown", "bob@example.com", "+33600000000",
            OffsetDateTime.now(), OffsetDateTime.now(), List.of());
        when(journalistRepository.save(any())).thenReturn(saved);

        Journalist result = service.create(command);

        assertThat(result.firstName()).isEqualTo("Bob");
        assertThat(result.lastName()).isEqualTo("Brown");
    }

    @Test
    void shouldRejectInvalidCreateCommandBeforeRepositoryInteraction() {
        CreateJournalistCommand command = new CreateJournalistCommand("  ", "Brown", null, null);

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
}
