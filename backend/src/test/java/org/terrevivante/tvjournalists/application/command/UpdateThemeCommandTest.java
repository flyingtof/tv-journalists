package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateThemeCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectMissingId() {
        UpdateThemeCommand command = new UpdateThemeCommand(null, "Politics");

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("id");
    }

    @Test
    void shouldRejectBlankName() {
        UpdateThemeCommand command = new UpdateThemeCommand(UUID.randomUUID(), "  ");

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("name");
    }
}
