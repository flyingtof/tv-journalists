package org.terrevivante.tvjournalists.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityTest {

    private static final Media A_MEDIA = new Media(UUID.randomUUID(), "France 2", MediaType.TV, null);

    private Activity anActivity(List<Theme> themes) {
        return new Activity(UUID.randomUUID(), UUID.randomUUID(), A_MEDIA, "Reporter", null, null, themes);
    }

    @Test
    void nullThemesIsNormalizedToEmptyList() {
        var activity = anActivity(null);

        assertThat(activity.themes()).isNotNull().isEmpty();
    }

    @Test
    void nonNullThemesIsStoredAsImmutableCopy() {
        var mutableThemes = new ArrayList<Theme>();
        var activity = anActivity(mutableThemes);

        mutableThemes.add(new Theme(UUID.randomUUID(), "Politique"));

        assertThat(activity.themes()).isEmpty();
    }

    @Test
    void storedThemesListIsUnmodifiable() {
        var activity = anActivity(new ArrayList<>());

        assertThatThrownBy(() -> activity.themes().add(new Theme(UUID.randomUUID(), "Politique")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void shouldRejectNullMedia() {
        assertThatThrownBy(() -> new Activity(UUID.randomUUID(), UUID.randomUUID(), null, "Reporter", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("media");
    }
}
