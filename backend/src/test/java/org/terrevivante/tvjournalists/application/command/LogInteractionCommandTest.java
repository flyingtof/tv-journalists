package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogInteractionCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectNullJournalistId() {
        LogInteractionCommand command = new LogInteractionCommand(null, null, LocalDate.now(), "some event", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("journalistId");
    }

    @Test
    void shouldRejectNullDate() {
        LogInteractionCommand command = new LogInteractionCommand(UUID.randomUUID(), null, null, "some event", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("date");
    }

    @Test
    void shouldRejectNullDescription() {
        LogInteractionCommand command = new LogInteractionCommand(UUID.randomUUID(), null, LocalDate.now(), null, null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("description");
    }

    @Test
    void shouldRejectBlankDescription() {
        LogInteractionCommand command = new LogInteractionCommand(UUID.randomUUID(), null, LocalDate.now(), "  ", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("description");
    }

    @Test
    void shouldAcceptValidCommandWithOptionalActivityId() {
        LogInteractionCommand cmd = new LogInteractionCommand(
            UUID.randomUUID(), null, LocalDate.now(), "Press conference", null);
        assertThat(validator.validate(cmd)).isEmpty();
        assertThat(cmd.description()).isEqualTo("Press conference");
    }

    @Test
    void shouldAcceptValidCommandWithActivityId() {
        LogInteractionCommand cmd = new LogInteractionCommand(
            UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), "Interview", null);
        assertThat(validator.validate(cmd)).isEmpty();
        assertThat(cmd.activityId()).isNotNull();
    }

    @Test
    void shouldRejectFutureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        LogInteractionCommand command =
            new LogInteractionCommand(UUID.randomUUID(), null, tomorrow, "Press conference", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("date");
    }
}
