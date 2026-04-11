package org.terrevivante.tvjournalists.api.controller;

import org.terrevivante.tvjournalists.domain.Theme;
import org.terrevivante.tvjournalists.persistence.ThemeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/themes")
public class ThemeController {

    private final ThemeRepository themeRepository;

    public ThemeController(ThemeRepository themeRepository) {
        this.themeRepository = themeRepository;
    }

    @GetMapping
    public List<Theme> getAllThemes() {
        return themeRepository.findAll();
    }
}

