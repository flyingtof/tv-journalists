package org.terrevivante.tvjournalists.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.terrevivante.tvjournalists.api.dto.MediaDTO;
import org.terrevivante.tvjournalists.application.usecase.ListMediaUseCase;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
public class MediaController {

    private final ListMediaUseCase listMediaUseCase;

    public MediaController(ListMediaUseCase listMediaUseCase) {
        this.listMediaUseCase = listMediaUseCase;
    }

    @GetMapping
    public List<MediaDTO> getAllMedia() {
        return listMediaUseCase.listMedia().stream()
            .map(m -> new MediaDTO(m.id(), m.name(), m.type(), m.url()))
            .toList();
    }
}
