package org.terrevivante.tvjournalists.application.service;

import org.terrevivante.tvjournalists.application.usecase.ListMediaUseCase;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;

import java.util.List;

public class ReferenceDataApplicationService implements ListMediaUseCase {

    private final MediaRepository mediaRepository;

    public ReferenceDataApplicationService(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @Override
    public List<Media> listMedia() {
        return mediaRepository.findAll();
    }
}
