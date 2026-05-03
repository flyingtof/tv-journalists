package org.terrevivante.tvjournalists.domain.query;

/**
 * Décrit un critère de tri: champ métier ciblé et sens du tri.
 */
public record SortOrder(String field, SortDirection direction) {
    public SortOrder {
        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException("sort field must not be blank");
        }
        if (direction == null) {
            throw new IllegalArgumentException("sort direction must not be null");
        }
    }
}
