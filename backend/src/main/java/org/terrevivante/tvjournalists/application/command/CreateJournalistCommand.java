package org.terrevivante.tvjournalists.application.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateJournalistCommand(
    @NotBlank
    String firstName,
    @NotBlank
    String lastName,
    @Email
    @Pattern(regexp = ".*\\S.*")
    String globalEmail,
    String globalPhone
) {
}
