package org.terrevivante.tvjournalists.domain.query;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalistSearchCriteriaTest {

    @Test
    void nullMediaIsNormalizedToEmptyList() {
        var criteria = new JournalistSearchCriteria("Alice", null, List.of("tech"));

        assertThat(criteria.media()).isNotNull().isEmpty();
    }

    @Test
    void nullThemesIsNormalizedToEmptyList() {
        var criteria = new JournalistSearchCriteria("Alice", List.of("TF1"), null);

        assertThat(criteria.themes()).isNotNull().isEmpty();
    }

    @Test
    void bothNullListsAreNormalizedToEmptyLists() {
        var criteria = new JournalistSearchCriteria("Alice", null, null);

        assertThat(criteria.media()).isNotNull().isEmpty();
        assertThat(criteria.themes()).isNotNull().isEmpty();
    }

    @Test
    void nonNullMediaIsStoredAsImmutableCopy() {
        var mutableMedia = new ArrayList<>(List.of("TF1", "France2"));
        var criteria = new JournalistSearchCriteria("Alice", mutableMedia, null);

        mutableMedia.add("M6");

        assertThat(criteria.media()).containsExactly("TF1", "France2");
    }

    @Test
    void nonNullThemesIsStoredAsImmutableCopy() {
        var mutableThemes = new ArrayList<>(List.of("politics", "tech"));
        var criteria = new JournalistSearchCriteria("Alice", null, mutableThemes);

        mutableThemes.add("sport");

        assertThat(criteria.themes()).containsExactly("politics", "tech");
    }

    @Test
    void storedMediaListIsUnmodifiable() {
        var criteria = new JournalistSearchCriteria("Alice", List.of("TF1"), List.of("tech"));

        assertThatThrownBy(() -> criteria.media().add("M6"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void storedThemesListIsUnmodifiable() {
        var criteria = new JournalistSearchCriteria("Alice", List.of("TF1"), List.of("tech"));

        assertThatThrownBy(() -> criteria.themes().add("sport"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
