package org.terrevivante.tvjournalists.api.controller;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.application.usecase.ListThemesUseCase;
import org.terrevivante.tvjournalists.domain.model.Theme;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ThemeControllerTest {

    private final ListThemesUseCase listThemesUseCase = mock(ListThemesUseCase.class);
    private final ThemeController controller = new ThemeController(listThemesUseCase);

    @Test
    void getAllThemes_delegatesToUseCase() {
        Theme theme = new Theme(UUID.randomUUID(), "Biodiversity");
        when(listThemesUseCase.listThemes()).thenReturn(List.of(theme));

        List<ThemeDTO> result = controller.getAllThemes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Biodiversity");
        verify(listThemesUseCase).listThemes();
    }
}
