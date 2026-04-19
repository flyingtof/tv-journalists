package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.lang.reflect.RecordComponent;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateUserCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRequireEnabledFlagLikeUpdateCommand() {
        RecordComponent enabled = CreateUserCommand.class.getRecordComponents()[4];
        CreateUserCommand command = new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            null,
            Set.of(Role.USER)
        );

        assertThat(enabled.getType()).isEqualTo(Boolean.class);
        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("enabled");
    }
}
