package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PersistenceJournalistMapper {

    Journalist toDomain(JournalistEntity entity);

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
}
