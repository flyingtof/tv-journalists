package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record JournalistActivityUpsertCommand(
    UUID id,
    @NotNull UUID mediaId,
    @Email @Pattern(regexp = ".*\\S.*") String specificEmail,
    String specificPhone,
    List<@NotNull UUID> themeIds
) {
    public static final String DEFAULT_ROLE = "Journaliste";

    public JournalistActivityUpsertCommand {
        themeIds = themeIds == null ? List.of() : List.copyOf(themeIds);
    }
}
