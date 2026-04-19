package org.terrevivante.tvjournalists.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.terrevivante.tvjournalists.api.dto.*;
import org.terrevivante.tvjournalists.api.mapper.JournalistMapper;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.LogInteractionUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistsUseCase;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;
import org.terrevivante.tvjournalists.domain.query.SortDirection;
import org.terrevivante.tvjournalists.domain.query.SortOrder;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journalists")
public class JournalistController {

    private final CreateJournalistUseCase createJournalistUseCase;
    private final GetJournalistUseCase getJournalistUseCase;
    private final SearchJournalistsUseCase searchJournalistsUseCase;
    private final LogInteractionUseCase logInteractionUseCase;
    private final JournalistMapper journalistMapper;

    public JournalistController(CreateJournalistUseCase createJournalistUseCase,
                                GetJournalistUseCase getJournalistUseCase,
                                SearchJournalistsUseCase searchJournalistsUseCase,
                                LogInteractionUseCase logInteractionUseCase,
                                JournalistMapper journalistMapper) {
        this.createJournalistUseCase = createJournalistUseCase;
        this.getJournalistUseCase = getJournalistUseCase;
        this.searchJournalistsUseCase = searchJournalistsUseCase;
        this.logInteractionUseCase = logInteractionUseCase;
        this.journalistMapper = journalistMapper;
    }

    @PostMapping
    public ResponseEntity<JournalistDTO> createJournalist(@RequestBody JournalistCreateDTO journalistCreateDTO) {
        Journalist savedJournalist = createJournalistUseCase.create(journalistMapper.toCommand(journalistCreateDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(journalistMapper.toDto(savedJournalist));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalistDTO> getJournalist(@PathVariable UUID id) {
        Journalist journalist = getJournalistUseCase.getById(id);
        return ResponseEntity.ok(journalistMapper.toDto(journalist));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JournalistDTO>> searchJournalists(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) List<String> media,
            @RequestParam(required = false) List<String> themes,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        JournalistSearchCriteria criteria = new JournalistSearchCriteria(
            name,
            media != null ? media : List.of(),
            themes != null ? themes : List.of()
        );
        // Read raw sort params without Spring's comma-splitting behaviour
        String[] rawSort = request.getParameterValues("sort");
        List<SortOrder> sortOrders = parseSortParams(
            rawSort != null ? Arrays.asList(rawSort) : List.of());
        PageRequest pageRequest = new PageRequest(page, size, sortOrders);
        PageResult<Journalist> results = searchJournalistsUseCase.search(criteria, pageRequest);
        PageResult<JournalistDTO> dtoResults = new PageResult<>(
            results.content().stream().map(journalistMapper::toDto).toList(),
            results.totalElements(),
            results.page(),
            results.size()
        );
        return ResponseEntity.ok(PageResponse.from(dtoResults));
    }

    private static final java.util.Set<String> ALLOWED_SORT_FIELDS = java.util.Set.of(
        "firstName", "lastName", "globalEmail", "globalPhone"
    );

    private static List<SortOrder> parseSortParams(List<String> sort) {
        if (sort == null || sort.isEmpty()) return List.of();
        return sort.stream()
            .filter(s -> s != null && !s.isBlank())
            .map(s -> {
                String[] parts = s.split(",", 2);
                String field = parts[0].trim();
                if (!ALLOWED_SORT_FIELDS.contains(field)) {
                    throw new IllegalArgumentException(
                        "Unknown sort field '" + field + "'. Allowed: " + ALLOWED_SORT_FIELDS);
                }
                SortDirection direction = (parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim()))
                    ? SortDirection.DESC
                    : SortDirection.ASC;
                return new SortOrder(field, direction);
            })
            .toList();
    }

    @PostMapping("/{id}/interactions")
    public ResponseEntity<InteractionDTO> logInteraction(
            @PathVariable UUID id,
            @RequestBody InteractionCreateDTO interactionCreateDTO) {
        InteractionLog savedLog = logInteractionUseCase.log(journalistMapper.toCommand(id, interactionCreateDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(journalistMapper.toDto(savedLog));
    }
}
