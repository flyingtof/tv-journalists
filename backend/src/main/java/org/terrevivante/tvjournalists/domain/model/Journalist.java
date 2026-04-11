package org.terrevivante.tvjournalists.domain.model;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Journalist(
    UUID id,
    String firstName,
    String lastName,
    String globalEmail,
    String globalPhone,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<Activity> activities
) {
    public Journalist {
        activities = activities == null ? List.of() : List.copyOf(activities);
    }
}
