package org.terrevivante.tvjournalists.application.command;

public record CreateJournalistCommand(
    String firstName,
    String lastName,
    String globalEmail,
    String globalPhone
) {
    public CreateJournalistCommand {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }
    }
}
