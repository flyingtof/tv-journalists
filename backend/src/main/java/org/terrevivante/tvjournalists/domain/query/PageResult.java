package org.terrevivante.tvjournalists.domain.query;

import java.util.List;

public record PageResult<T>(
    List<T> content,
    long totalElements,
    int page,
    int size
) {
    public PageResult {
        if (totalElements < 0) throw new IllegalArgumentException("totalElements must be >= 0");
        if (page < 0) throw new IllegalArgumentException("page must be >= 0");
        if (size <= 0) throw new IllegalArgumentException("size must be > 0");
        content = content == null ? List.of() : List.copyOf(content);
    }
}
