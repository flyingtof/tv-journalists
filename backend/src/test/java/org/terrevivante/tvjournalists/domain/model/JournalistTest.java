package org.terrevivante.tvjournalists.domain.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalistTest {

    private Journalist aJournalist(List<Activity> activities) {
        return new Journalist(
                UUID.randomUUID(), "Alice", "Dupont", "alice@tf1.fr", "+33600000000",
                OffsetDateTime.now(), OffsetDateTime.now(),
                activities
        );
    }

    // --- activities ---

    @Test
    void nullActivitiesIsNormalizedToEmptyList() {
        var journalist = aJournalist(null);

        assertThat(journalist.activities()).isNotNull().isEmpty();
    }

    @Test
    void nonNullActivitiesIsStoredAsImmutableCopy() {
        var mutableActivities = new ArrayList<Activity>();
        var journalist = aJournalist(mutableActivities);

        mutableActivities.add(anActivity());

        assertThat(journalist.activities()).isEmpty();
    }

    @Test
    void storedActivitiesListIsUnmodifiable() {
        var journalist = aJournalist(new ArrayList<>());

        assertThatThrownBy(() -> journalist.activities().add(anActivity()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private Activity anActivity() {
        Media media = new Media(UUID.randomUUID(), "Le Monde", MediaType.PRESS, null);
        return new Activity(UUID.randomUUID(), UUID.randomUUID(), media, "Reporter", null, null, List.of());
    }
}
