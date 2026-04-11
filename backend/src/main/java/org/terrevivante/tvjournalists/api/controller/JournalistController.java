package org.terrevivante.tvjournalists.api.controller;

import org.terrevivante.tvjournalists.api.dto.*;
import org.terrevivante.tvjournalists.api.mapper.JournalistMapper;
import org.terrevivante.tvjournalists.domain.InteractionLog;
import org.terrevivante.tvjournalists.domain.InteractionService;
import org.terrevivante.tvjournalists.domain.Journalist;
import org.terrevivante.tvjournalists.domain.JournalistService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/journalists")
public class JournalistController {

    private final JournalistService journalistService;
    private final InteractionService interactionService;
    private final JournalistMapper journalistMapper;

    public JournalistController(JournalistService journalistService, 
                                InteractionService interactionService,
                                JournalistMapper journalistMapper) {
        this.journalistService = journalistService;
        this.interactionService = interactionService;
        this.journalistMapper = journalistMapper;
    }

    @PostMapping
    public ResponseEntity<JournalistDTO> createJournalist(@Valid @RequestBody JournalistCreateDTO journalistCreateDTO) {
        Journalist journalist = journalistMapper.toEntity(journalistCreateDTO);
        Journalist savedJournalist = journalistService.save(journalist);
        return ResponseEntity.status(HttpStatus.CREATED).body(journalistMapper.toDto(savedJournalist));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalistDTO> getJournalist(@PathVariable UUID id) {
        return journalistService.findById(id)
            .map(journalistMapper::toDto)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<Page<JournalistDTO>> searchJournalists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> media,
            @RequestParam(required = false) List<String> themes,
            Pageable pageable) {
        Page<Journalist> results = journalistService.search(name, media, themes, pageable);
        return ResponseEntity.ok(results.map(journalistMapper::toDto));
    }

    @PostMapping("/{id}/interactions")
    public ResponseEntity<InteractionDTO> logInteraction(
            @PathVariable UUID id,
            @Valid @RequestBody InteractionCreateDTO interactionCreateDTO) {
        InteractionLog log = journalistMapper.toEntity(interactionCreateDTO);
        InteractionLog savedLog = interactionService.logInteraction(id, log);
        return ResponseEntity.status(HttpStatus.CREATED).body(journalistMapper.toDto(savedLog));
    }
}
