package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class ThemeRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Test
    void shouldRejectUnknownExplicitIdWhenSavingTheme() {
        UUID requestedId = UUID.randomUUID();

        assertThatThrownBy(() -> themeRepository.save(new Theme(requestedId, "Climate")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(requestedId.toString());
    }
}
