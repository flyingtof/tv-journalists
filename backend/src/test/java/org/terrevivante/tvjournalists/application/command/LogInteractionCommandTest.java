package org.terrevivante.tvjournalists.application.command;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LogInteractionCommandTest {

    @Test
    void shouldRejectNullJournalistId() {
        assertThatThrownBy(() -> new LogInteractionCommand(null, null, LocalDate.now(), "some event", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("journalistId");
    }

    @Test
    void shouldRejectNullDate() {
        assertThatThrownBy(() -> new LogInteractionCommand(UUID.randomUUID(), null, null, "some event", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("date");
    }

    @Test
    void shouldRejectNullDescription() {
        assertThatThrownBy(() -> new LogInteractionCommand(UUID.randomUUID(), null, LocalDate.now(), null, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("description");
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> new LogInteractionCommand(UUID.randomUUID(), null, LocalDate.now(), "  ", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("description");
    }

    @Test
    void shouldAcceptValidCommandWithOptionalActivityId() {
        LogInteractionCommand cmd = new LogInteractionCommand(
            UUID.randomUUID(), null, LocalDate.now(), "Press conference", null);
        assertThat(cmd.description()).isEqualTo("Press conference");
    }

    @Test
    void shouldAcceptValidCommandWithActivityId() {
        LogInteractionCommand cmd = new LogInteractionCommand(
            UUID.randomUUID(), UUID.randomUUID(), LocalDate.now(), "Interview", null);
        assertThat(cmd.activityId()).isNotNull();
    }

    @Test
    void shouldRejectFutureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> new LogInteractionCommand(UUID.randomUUID(), null, tomorrow, "Press conference", null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("future");
    }
}
