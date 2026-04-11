package org.terrevivante.tvjournalists.api.controller;

import org.terrevivante.tvjournalists.domain.Media;
import org.terrevivante.tvjournalists.persistence.MediaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaRepository mediaRepository;

    public MediaController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @GetMapping
    public List<Media> getAllMedia() {
        return mediaRepository.findAll();
    }
}
