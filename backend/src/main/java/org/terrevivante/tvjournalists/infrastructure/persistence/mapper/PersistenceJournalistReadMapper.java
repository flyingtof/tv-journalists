package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.terrevivante.tvjournalists.application.readmodel.ActivityView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.application.readmodel.MediaView;
import org.terrevivante.tvjournalists.application.readmodel.ThemeView;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PersistenceJournalistReadMapper {

    @Mapping(source = "activities", target = "activities", qualifiedByName = "toMergedActivities")
    JournalistListItemView toListItemView(JournalistEntity entity);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<JournalistListItemView> toListItemViews(List<JournalistEntity> entities);

    @Mapping(source = "activities", target = "activities", qualifiedByName = "toMergedActivities")
    JournalistProfileView toProfileView(JournalistEntity entity);

    MediaView toView(MediaEntity entity);

    ThemeView toView(ThemeEntity entity);

    @Mapping(source = "themes", target = "themes")
    ActivityView toView(ActivityEntity entity);

    /**
     * Attaches theme entities (fetched separately) to the activities already loaded on each journalist.
     * This avoids N+1 when the theme collection is lazily loaded after pagination.
     */
    default void attachThemes(List<JournalistEntity> journalists, List<ActivityEntity> activitiesWithThemes) {
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

    @Named("toMergedActivities")
    default List<ActivityView> toMergedActivities(List<ActivityEntity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }

        return List.copyOf(activities.stream()
            .map(this::toView)
            .collect(Collectors.toMap(
                activity -> activity.media().id(),
                Function.identity(),
                this::mergeActivities,
                LinkedHashMap::new
            ))
            .values());
    }

    private ActivityView mergeActivities(ActivityView existing, ActivityView activity) {
        return new ActivityView(
            existing.id(),
            existing.media(),
            firstNonBlank(existing.role(), activity.role()),
            firstNonBlank(existing.specificEmail(), activity.specificEmail()),
            firstNonBlank(existing.specificPhone(), activity.specificPhone()),
            mergeThemes(existing.themes(), activity.themes())
        );
    }

    private List<ThemeView> mergeThemes(List<ThemeView> left, List<ThemeView> right) {
        Map<String, ThemeView> themesByKey = new LinkedHashMap<>();
        left.forEach(theme -> themesByKey.put(themeKey(theme), theme));
        right.forEach(theme -> themesByKey.putIfAbsent(themeKey(theme), theme));
        return List.copyOf(themesByKey.values());
    }

    private String themeKey(ThemeView theme) {
        if (theme.id() != null) {
            return theme.id().toString();
        }
        return "name:" + theme.name();
    }

    private String firstNonBlank(String left, String right) {
        if (left != null && !left.isBlank()) {
            return left;
        }
        return right;
    }
}

