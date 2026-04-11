package org.terrevivante.tvjournalists.domain.query;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResultTest {

    @Test
    void nullContentIsNormalizedToEmptyList() {
        var result = new PageResult<String>(null, 0L, 0, 10);

        assertThat(result.content()).isNotNull().isEmpty();
    }

    @Test
    void nonNullContentIsStoredAsImmutableCopy() {
        var mutableContent = new ArrayList<>(List.of("item1", "item2"));
        var result = new PageResult<>(mutableContent, 2L, 0, 10);

        mutableContent.add("item3");

        assertThat(result.content()).containsExactly("item1", "item2");
    }

    @Test
    void storedContentListIsUnmodifiable() {
        var result = new PageResult<>(List.of("item1"), 1L, 0, 10);

        assertThatThrownBy(() -> result.content().add("item2"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void negativeTotalElementsIsRejected() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), -1L, 0, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("totalElements");
    }

    @Test
    void negativePageIsRejected() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0L, -1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("page");
    }

    @Test
    void zeroSizeIsRejected() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0L, 0, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }

    @Test
    void negativeSizeIsRejected() {
        assertThatThrownBy(() -> new PageResult<>(List.of(), 0L, 0, -5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("size");
    }
}
