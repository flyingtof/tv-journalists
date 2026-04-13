package org.terrevivante.tvjournalists.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.application.usecase.ListThemesUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/themes")
public class ThemeController {

    private final ListThemesUseCase listThemesUseCase;

    public ThemeController(ListThemesUseCase listThemesUseCase) {
        this.listThemesUseCase = listThemesUseCase;
    }

    @GetMapping
    public List<ThemeDTO> getAllThemes() {
        return listThemesUseCase.listThemes().stream()
            .map(t -> new ThemeDTO(t.id(), t.name()))
            .toList();
    }
}
