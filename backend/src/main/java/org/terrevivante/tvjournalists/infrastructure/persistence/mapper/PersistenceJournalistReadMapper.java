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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PersistenceJournalistReadMapper {

    @Mapping(source = "activities", target = "mediaNames", qualifiedByName = "toDistinctMediaNames")
    JournalistListItemView toListItemView(JournalistEntity entity);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<JournalistListItemView> toListItemViews(List<JournalistEntity> entities);

    @Mapping(source = "activities", target = "activities", qualifiedByName = "toMergedProfileActivities")
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
        JournalistMapperSupport.attachThemes(journalists, activitiesWithThemes);
    }

    @Named("toDistinctMediaNames")
    default List<String> toDistinctMediaNames(List<ActivityEntity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }

        return List.copyOf(activities.stream()
            .map(ActivityEntity::getMedia)
            .filter(media -> media != null && media.getName() != null && !media.getName().isBlank())
            .map(MediaEntity::getName)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Named("toMergedProfileActivities")
    default List<ActivityView> toMergedProfileActivities(List<ActivityEntity> activities) {
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
            JournalistMapperSupport.firstNonBlank(existing.role(), activity.role()),
            JournalistMapperSupport.firstNonBlank(existing.specificEmail(), activity.specificEmail()),
            JournalistMapperSupport.firstNonBlank(existing.specificPhone(), activity.specificPhone()),
            mergeThemes(existing.themes(), activity.themes())
        );
    }

    private List<ThemeView> mergeThemes(List<ThemeView> left, List<ThemeView> right) {
        return JournalistMapperSupport.mergeByStableKey(left, right, this::themeKey);
    }

    private String themeKey(ThemeView theme) {
        return JournalistMapperSupport.themeKey(theme.id(), theme.name());
    }
}

