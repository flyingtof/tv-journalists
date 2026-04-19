package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CreateJournalistCommandTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldRejectNullFirstName() {
        CreateJournalistCommand command = new CreateJournalistCommand(null, "Brown", null, null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("firstName");
    }

    @Test
    void shouldRejectBlankFirstName() {
        CreateJournalistCommand command = new CreateJournalistCommand("  ", "Brown", null, null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("firstName");
    }

    @Test
    void shouldRejectNullLastName() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", null, null, null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("lastName");
    }

    @Test
    void shouldRejectBlankLastName() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "  ", null, null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("lastName");
    }

    @Test
    void shouldRejectMalformedGlobalEmail() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", "not-an-email", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .containsExactly("globalEmail");
    }

    @Test
    void shouldRejectEmptyGlobalEmail() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", "", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .contains("globalEmail");
    }

    @Test
    void shouldRejectBlankGlobalEmail() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", "   ", null);

        assertThat(validator.validate(command))
            .extracting(violation -> violation.getPropertyPath().toString())
            .contains("globalEmail");
    }

    @Test
    void shouldAcceptNullGlobalEmail() {
        CreateJournalistCommand command = new CreateJournalistCommand("Bob", "Brown", null, "+33600000000");

        assertThat(validator.validate(command)).isEmpty();
        assertThat(command.globalEmail()).isNull();
    }

    @Test
    void shouldAcceptValidCommand() {
        CreateJournalistCommand cmd = new CreateJournalistCommand("Bob", "Brown", "bob@example.com", "+33600000000");

        assertThat(validator.validate(cmd)).isEmpty();
        assertThat(cmd.firstName()).isEqualTo("Bob");
    }
}
