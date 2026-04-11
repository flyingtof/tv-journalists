package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import org.terrevivante.tvjournalists.domain.model.*;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PersistenceJournalistMapper {

    public Journalist toDomain(JournalistEntity entity) {
        List<Activity> activities = entity.getActivities().stream()
            .map(this::toDomain)
            .toList();
        return new Journalist(
            entity.getId(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.getGlobalEmail(),
            entity.getGlobalPhone(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            activities
        );
    }

    /** Maps a page of journalist entities (with activities+media loaded) to domain records. */
    public List<Journalist> toDomainList(List<JournalistEntity> entities) {
        return entities.stream()
            .map(this::toDomain)
            .toList();
    }

    public Activity toDomain(ActivityEntity entity) {
        List<Theme> themes = entity.getThemes().stream()
            .map(this::toDomain)
            .toList();
        return new Activity(
            entity.getId(),
            entity.getJournalist() != null ? entity.getJournalist().getId() : null,
            toDomain(entity.getMedia()),
            entity.getRole(),
            entity.getSpecificEmail(),
            entity.getSpecificPhone(),
            themes
        );
    }

    public Media toDomain(MediaEntity entity) {
        return new Media(entity.getId(), entity.getName(), entity.getType(), entity.getUrl());
    }

    public Theme toDomain(ThemeEntity entity) {
        return new Theme(entity.getId(), entity.getName());
    }

    public InteractionLog toDomain(InteractionLogEntity entity) {
        return new InteractionLog(
            entity.getId(),
            entity.getJournalistId(),
            entity.getActivityId(),
            entity.getDate(),
            entity.getDescription(),
            entity.getCreatedBy(),
            entity.getCreatedAt()
        );
    }

    public JournalistEntity toEntity(Journalist journalist) {
        JournalistEntity entity = new JournalistEntity();
        entity.setId(journalist.id());
        entity.setFirstName(journalist.firstName());
        entity.setLastName(journalist.lastName());
        entity.setGlobalEmail(journalist.globalEmail());
        entity.setGlobalPhone(journalist.globalPhone());
        return entity;
    }

    public InteractionLogEntity toEntity(InteractionLog log) {
        InteractionLogEntity entity = new InteractionLogEntity();
        entity.setId(log.id());
        entity.setJournalistId(log.journalistId());
        entity.setActivityId(log.activityId());
        entity.setDate(log.date());
        entity.setDescription(log.description());
        entity.setCreatedBy(log.createdBy());
        return entity;
    }

    /**
     * Attaches theme entities (fetched separately) to the activities already loaded on each journalist.
     * This avoids N+1 when the theme collection is lazily loaded after pagination.
     */
    public void attachThemes(List<JournalistEntity> journalists, List<ActivityEntity> activitiesWithThemes) {
        Map<java.util.UUID, ActivityEntity> byId = activitiesWithThemes.stream()
            .collect(Collectors.toMap(ActivityEntity::getId, a -> a));
        for (JournalistEntity j : journalists) {
            j.getActivities().forEach(a -> {
                ActivityEntity withThemes = byId.get(a.getId());
                if (withThemes != null) {
                    a.setThemes(withThemes.getThemes());
                }
            });
        }
    }
}
