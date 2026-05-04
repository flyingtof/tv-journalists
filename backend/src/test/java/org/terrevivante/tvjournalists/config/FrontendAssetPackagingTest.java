package org.terrevivante.tvjournalists.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class FrontendAssetPackagingTest {

    @Test
    void shouldExposeBuiltFrontendIndexOnClasspath() {
        assertThat(new ClassPathResource("static/index.html").exists())
            .as("frontend build should be copied into backend static resources")
            .isTrue();
    }
}
