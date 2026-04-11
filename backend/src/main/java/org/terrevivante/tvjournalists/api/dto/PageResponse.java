package org.terrevivante.tvjournalists.api.dto;

import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.List;

/**
 * Spring-Page-compatible HTTP response shape for paginated results.
 * Maps the Spring-free domain {@link PageResult} to the JSON shape the frontend expects.
 */
public record PageResponse<T>(
    List<T> content,
    long totalElements,
    int totalPages,
    int number,
    int size,
    boolean first,
    boolean last,
    int numberOfElements,
    boolean empty
) {
    public static <T> PageResponse<T> from(PageResult<T> result) {
        int pages = result.size() == 0 ? 1
            : Math.max(1, (int) Math.ceil((double) result.totalElements() / result.size()));
        boolean isFirst = result.page() == 0;
        boolean isLast = result.page() >= pages - 1;
        int numElements = result.content().size();
        return new PageResponse<>(
            result.content(),
            result.totalElements(),
            pages,
            result.page(),
            result.size(),
            isFirst,
            isLast,
            numElements,
            numElements == 0
        );
    }
}
