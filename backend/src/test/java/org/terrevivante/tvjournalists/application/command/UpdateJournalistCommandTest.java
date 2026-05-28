package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateJournalistCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectUpdateWithoutJournalistId() {
        UpdateJournalistCommand command = new UpdateJournalistCommand(
            null,
            "Alice",
            "Martin",
            "alice@example.com",
            "+33123456789",
            List.of()
        );

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("id");
    }

    @Test
    void shouldRejectUpdateWithoutActivityMediaId() {
        UpdateJournalistCommand command = new UpdateJournalistCommand(
            UUID.randomUUID(),
            "Alice",
            "Martin",
            "alice@example.com",
            "+33123456789",
            List.of(new JournalistActivityUpsertCommand(null, null, "Presenter", null, null, List.of()))
        );

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .contains("activities[0].mediaId");
    }

    @Test
    void shouldRejectBlankFirstName() {
        UpdateJournalistCommand command = new UpdateJournalistCommand(
            UUID.randomUUID(), "  ", "Martin", null, null, List.of()
        );

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("firstName");
    }

    @Test
    void shouldRejectMalformedGlobalEmail() {
        UpdateJournalistCommand command = new UpdateJournalistCommand(
            UUID.randomUUID(), "Alice", "Martin", "not-an-email", null, List.of()
        );

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("globalEmail");
    }

    @Test
    void shouldAcceptValidUpdateCommand() {
        UpdateJournalistCommand command = new UpdateJournalistCommand(
            UUID.randomUUID(),
            "Alice",
            "Martin",
            "alice@example.com",
            null,
            List.of()
        );

        assertThat(validator.validate(command)).isEmpty();
    }
}
