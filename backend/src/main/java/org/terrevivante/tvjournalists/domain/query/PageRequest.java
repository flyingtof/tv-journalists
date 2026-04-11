package org.terrevivante.tvjournalists.domain.query;

import java.util.List;

public record PageRequest(
    int page,
    int size,
    List<SortOrder> sortOrders
) {
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0, got: " + page);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0, got: " + size);
        }
        sortOrders = sortOrders != null ? List.copyOf(sortOrders) : List.of();
    }

    public PageRequest(int page, int size) {
        this(page, size, List.of());
    }
}
