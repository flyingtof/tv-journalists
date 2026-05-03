package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.InteractionLogEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PersistenceJournalistMapper {

    @Mapping(source = "activities", target = "activities", qualifiedByName = "toMergedActivities")
    Journalist toDomain(JournalistEntity entity);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<Journalist> toDomainList(List<JournalistEntity> entities);

    @Mapping(source = "journalist.id", target = "journalistId")
    Activity toDomain(ActivityEntity entity);

    Media toDomain(MediaEntity entity);

    Theme toDomain(ThemeEntity entity);

    InteractionLog toDomain(InteractionLogEntity entity);

    InteractionLogEntity toEntity(InteractionLog log);

    /**
     * Attaches theme entities (fetched separately) to the activities already loaded on each journalist.
     * This avoids N+1 when the theme collection is lazily loaded after pagination.
     */
    default void attachThemes(List<JournalistEntity> journalists, List<ActivityEntity> activitiesWithThemes) {
        JournalistMapperSupport.attachThemes(journalists, activitiesWithThemes);
    }

    @Named("toMergedActivities")
    default List<Activity> toMergedActivities(List<ActivityEntity> activities) {
        if (activities == null || activities.isEmpty()) {
            return List.of();
        }

        return List.copyOf(activities.stream()
            .map(this::toDomain)
            .collect(Collectors.toMap(
                activity -> activity.media().id(),
                Function.identity(),
                this::mergeActivities,
                LinkedHashMap::new
            ))
            .values());
    }

    private Activity mergeActivities(Activity existing, Activity activity) {
        return new Activity(
            existing.id(),
            existing.journalistId(),
            existing.media(),
            JournalistMapperSupport.firstNonBlank(existing.role(), activity.role()),
            JournalistMapperSupport.firstNonBlank(existing.specificEmail(), activity.specificEmail()),
            JournalistMapperSupport.firstNonBlank(existing.specificPhone(), activity.specificPhone()),
            mergeThemes(existing.themes(), activity.themes())
        );
    }

    private List<Theme> mergeThemes(List<Theme> left, List<Theme> right) {
        return JournalistMapperSupport.mergeByStableKey(left, right, this::themeKey);
    }

    private String themeKey(Theme theme) {
        return JournalistMapperSupport.themeKey(theme.id(), theme.name());
    }
}
