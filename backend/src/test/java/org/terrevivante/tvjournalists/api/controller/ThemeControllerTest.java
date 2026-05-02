package org.terrevivante.tvjournalists.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.api.dto.ThemeUpsertDTO;
import org.terrevivante.tvjournalists.application.command.CreateThemeCommand;
import org.terrevivante.tvjournalists.application.command.UpdateThemeCommand;
import org.terrevivante.tvjournalists.application.usecase.CreateThemeUseCase;
import org.terrevivante.tvjournalists.application.usecase.DeleteThemeUseCase;
import org.terrevivante.tvjournalists.application.usecase.ListThemesUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateThemeUseCase;
import org.terrevivante.tvjournalists.domain.model.Theme;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThemeControllerTest {

    private final CreateThemeUseCase createThemeUseCase = mock(CreateThemeUseCase.class);
    private final UpdateThemeUseCase updateThemeUseCase = mock(UpdateThemeUseCase.class);
    private final DeleteThemeUseCase deleteThemeUseCase = mock(DeleteThemeUseCase.class);
    private final ListThemesUseCase listThemesUseCase = mock(ListThemesUseCase.class);
    private final ThemeController controller = new ThemeController(
        listThemesUseCase,
        createThemeUseCase,
        updateThemeUseCase,
        deleteThemeUseCase
    );

    @Test
    void getAllThemes_delegatesToUseCase() {
        Theme theme = new Theme(UUID.randomUUID(), "Biodiversity");
        when(listThemesUseCase.listThemes()).thenReturn(List.of(theme));

        List<ThemeDTO> result = controller.getAllThemes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Biodiversity");
        verify(listThemesUseCase).listThemes();
    }

    @Test
    void createTheme_delegatesToUseCase() {
        Theme theme = new Theme(UUID.randomUUID(), "Biodiversity");
        when(createThemeUseCase.create(new CreateThemeCommand("Biodiversity"))).thenReturn(theme);

        ResponseEntity<ThemeDTO> response = controller.createTheme(new ThemeUpsertDTO("Biodiversity"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new ThemeDTO(theme.id(), "Biodiversity"));
        verify(createThemeUseCase).create(new CreateThemeCommand("Biodiversity"));
    }

    @Test
    void updateTheme_delegatesToUseCase() {
        UUID id = UUID.randomUUID();
        Theme theme = new Theme(id, "Biodiversity");
        when(updateThemeUseCase.update(new UpdateThemeCommand(id, "Biodiversity"))).thenReturn(theme);

        ThemeDTO response = controller.updateTheme(id, new ThemeUpsertDTO("Biodiversity"));

        assertThat(response).isEqualTo(new ThemeDTO(id, "Biodiversity"));
        verify(updateThemeUseCase).update(new UpdateThemeCommand(id, "Biodiversity"));
    }

    @Test
    void deleteTheme_delegatesToUseCase() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = controller.deleteTheme(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(deleteThemeUseCase).delete(id);
    }
}
