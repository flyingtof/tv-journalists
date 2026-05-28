package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record UpdateJournalistCommand(
    @NotNull UUID id,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @Email @Pattern(regexp = ".*\\S.*") String globalEmail,
    String globalPhone,
    List<@Valid JournalistActivityUpsertCommand> activities
) {}
