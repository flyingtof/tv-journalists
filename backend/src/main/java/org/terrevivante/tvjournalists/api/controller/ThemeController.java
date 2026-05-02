package org.terrevivante.tvjournalists.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

@RestController
@RequestMapping("/api/v1/themes")
public class ThemeController {

    private final ListThemesUseCase listThemesUseCase;
    private final CreateThemeUseCase createThemeUseCase;
    private final UpdateThemeUseCase updateThemeUseCase;
    private final DeleteThemeUseCase deleteThemeUseCase;

    public ThemeController(ListThemesUseCase listThemesUseCase,
                           CreateThemeUseCase createThemeUseCase,
                           UpdateThemeUseCase updateThemeUseCase,
                           DeleteThemeUseCase deleteThemeUseCase) {
        this.listThemesUseCase = listThemesUseCase;
        this.createThemeUseCase = createThemeUseCase;
        this.updateThemeUseCase = updateThemeUseCase;
        this.deleteThemeUseCase = deleteThemeUseCase;
    }

    @GetMapping
    public List<ThemeDTO> getAllThemes() {
        return listThemesUseCase.listThemes().stream()
            .map(this::toDto)
            .toList();
    }

    @PostMapping
    public ResponseEntity<ThemeDTO> createTheme(@RequestBody ThemeUpsertDTO themeUpsertDTO) {
        Theme createdTheme = createThemeUseCase.create(new CreateThemeCommand(themeUpsertDTO.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(createdTheme));
    }

    @PutMapping("/{id}")
    public ThemeDTO updateTheme(@PathVariable UUID id, @RequestBody ThemeUpsertDTO themeUpsertDTO) {
        return toDto(updateThemeUseCase.update(new UpdateThemeCommand(id, themeUpsertDTO.name())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTheme(@PathVariable UUID id) {
        deleteThemeUseCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ThemeDTO toDto(Theme theme) {
        return new ThemeDTO(theme.id(), theme.name());
    }
}
