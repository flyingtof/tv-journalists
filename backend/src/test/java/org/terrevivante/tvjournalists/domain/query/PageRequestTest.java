package org.terrevivante.tvjournalists.domain.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageRequestTest {

    @Test
    void shouldCreateValidPageRequest() {
        PageRequest request = new PageRequest(0, 1);
        assertThat(request.page()).isEqualTo(0);
        assertThat(request.size()).isEqualTo(1);
    }

    @Test
    void shouldCreateValidPageRequestWithPositivePage() {
        PageRequest request = new PageRequest(5, 20);
        assertThat(request.page()).isEqualTo(5);
        assertThat(request.size()).isEqualTo(20);
    }

    @Test
    void shouldRejectNegativePage() {
        assertThatThrownBy(() -> new PageRequest(-1, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("page");
    }

    @Test
    void shouldRejectZeroSize() {
        assertThatThrownBy(() -> new PageRequest(0, 0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size");
    }

    @Test
    void shouldRejectNegativeSize() {
        assertThatThrownBy(() -> new PageRequest(0, -5))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("size");
    }
}
