package org.terrevivante.tvjournalists.api.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.terrevivante.tvjournalists.api.dto.InteractionCreateDTO;
import org.terrevivante.tvjournalists.api.dto.InteractionDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistListItemDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistProfileDTO;
import org.terrevivante.tvjournalists.api.dto.PageResponse;
import org.terrevivante.tvjournalists.api.mapper.JournalistMapper;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistProfileUseCase;
import org.terrevivante.tvjournalists.application.usecase.LogInteractionUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistListUseCase;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
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
    private final GetJournalistProfileUseCase getJournalistProfileUseCase;
    private final SearchJournalistListUseCase searchJournalistListUseCase;
    private final LogInteractionUseCase logInteractionUseCase;
    private final JournalistMapper journalistMapper;

    public JournalistController(CreateJournalistUseCase createJournalistUseCase,
                                GetJournalistProfileUseCase getJournalistProfileUseCase,
                                SearchJournalistListUseCase searchJournalistListUseCase,
                                LogInteractionUseCase logInteractionUseCase,
                                JournalistMapper journalistMapper) {
        this.createJournalistUseCase = createJournalistUseCase;
        this.getJournalistProfileUseCase = getJournalistProfileUseCase;
        this.searchJournalistListUseCase = searchJournalistListUseCase;
        this.logInteractionUseCase = logInteractionUseCase;
        this.journalistMapper = journalistMapper;
    }

    @PostMapping
    public ResponseEntity<JournalistProfileDTO> createJournalist(@RequestBody JournalistCreateDTO journalistCreateDTO) {
        Journalist savedJournalist = createJournalistUseCase.create(journalistMapper.toCommand(journalistCreateDTO));
        return ResponseEntity.status(HttpStatus.CREATED).body(journalistMapper.toProfileDto(savedJournalist));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JournalistProfileDTO> getJournalist(@PathVariable UUID id) {
        JournalistProfileView journalist = getJournalistProfileUseCase.getById(id);
        return ResponseEntity.ok(journalistMapper.toProfileDto(journalist));
    }

    @GetMapping
    public ResponseEntity<PageResponse<JournalistListItemDTO>> searchJournalists(
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
        PageResult<JournalistListItemView> results = searchJournalistListUseCase.search(criteria, pageRequest);
        PageResult<JournalistListItemDTO> dtoResults = new PageResult<>(
            results.content().stream().map(journalistMapper::toListItemDto).toList(),
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
                SortDirection direction = SortDirection.ASC;
                if (parts.length > 1) {
                    String rawDirection = parts[1].trim();
                    if (rawDirection.isEmpty() || "asc".equalsIgnoreCase(rawDirection)) {
                        direction = SortDirection.ASC;
                    } else if ("desc".equalsIgnoreCase(rawDirection)) {
                        direction = SortDirection.DESC;
                    } else {
                        throw new IllegalArgumentException(
                            "Unknown sort direction '" + rawDirection + "'. Allowed: asc, desc");
                    }
                }
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
