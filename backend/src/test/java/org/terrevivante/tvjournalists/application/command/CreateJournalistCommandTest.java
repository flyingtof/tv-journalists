package org.terrevivante.tvjournalists.application.command;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateJournalistCommandTest {

    @Test
    void shouldRejectNullFirstName() {
        assertThatThrownBy(() -> new CreateJournalistCommand(null, "Brown", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("firstName");
    }

    @Test
    void shouldRejectBlankFirstName() {
        assertThatThrownBy(() -> new CreateJournalistCommand("  ", "Brown", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("firstName");
    }

    @Test
    void shouldRejectNullLastName() {
        assertThatThrownBy(() -> new CreateJournalistCommand("Bob", null, null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lastName");
    }

    @Test
    void shouldRejectBlankLastName() {
        assertThatThrownBy(() -> new CreateJournalistCommand("Bob", "  ", null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("lastName");
    }

    @Test
    void shouldAcceptValidCommand() {
        CreateJournalistCommand cmd = new CreateJournalistCommand("Bob", "Brown", "bob@example.com", "+33600000000");
        assertThat(cmd.firstName()).isEqualTo("Bob");
    }
}
