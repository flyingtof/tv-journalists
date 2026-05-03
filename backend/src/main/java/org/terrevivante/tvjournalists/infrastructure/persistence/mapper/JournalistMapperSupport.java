package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

final class JournalistMapperSupport {

    private JournalistMapperSupport() {
    }

    static void attachThemes(List<JournalistEntity> journalists, List<ActivityEntity> activitiesWithThemes) {
        Map<UUID, ActivityEntity> byId = activitiesWithThemes.stream()
            .collect(Collectors.toMap(ActivityEntity::getId, activity -> activity));
        for (JournalistEntity journalist : journalists) {
            journalist.getActivities().forEach(activity -> {
                ActivityEntity withThemes = byId.get(activity.getId());
                if (withThemes != null) {
                    activity.setThemes(withThemes.getThemes());
                }
            });
        }
    }

    static <T> List<T> mergeByStableKey(List<T> left, List<T> right, Function<T, String> keyExtractor) {
        Map<String, T> valuesByKey = new LinkedHashMap<>();
        left.forEach(value -> valuesByKey.put(keyExtractor.apply(value), value));
        right.forEach(value -> valuesByKey.putIfAbsent(keyExtractor.apply(value), value));
        return List.copyOf(valuesByKey.values());
    }

    static String themeKey(UUID id, String name) {
        if (id != null) {
            return id.toString();
        }
        return "name:" + name;
    }

    static String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return right;
    }
}

