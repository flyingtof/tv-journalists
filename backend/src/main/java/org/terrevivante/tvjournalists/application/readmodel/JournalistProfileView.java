package org.terrevivante.tvjournalists.application.readmodel;

import java.util.List;
import java.util.UUID;

public record JournalistProfileView(
    UUID id,
    String firstName,
    String lastName,
    String globalEmail,
    String globalPhone,
    List<ActivityView> activities
) {
    public JournalistProfileView {
        activities = activities == null ? List.of() : List.copyOf(activities);
    }
}

