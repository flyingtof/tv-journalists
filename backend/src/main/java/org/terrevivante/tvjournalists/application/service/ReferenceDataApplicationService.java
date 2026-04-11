package org.terrevivante.tvjournalists.application.service;

import org.terrevivante.tvjournalists.application.usecase.ListMediaUseCase;
import org.terrevivante.tvjournalists.application.usecase.ListThemesUseCase;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

import java.util.List;

public class ReferenceDataApplicationService implements ListMediaUseCase, ListThemesUseCase {

    private final MediaRepository mediaRepository;
    private final ThemeRepository themeRepository;

    public ReferenceDataApplicationService(MediaRepository mediaRepository, ThemeRepository themeRepository) {
        this.mediaRepository = mediaRepository;
        this.themeRepository = themeRepository;
    }

    @Override
    public List<Media> listMedia() {
        return mediaRepository.findAll();
    }

    @Override
    public List<Theme> listThemes() {
        return themeRepository.findAll();
    }
}
