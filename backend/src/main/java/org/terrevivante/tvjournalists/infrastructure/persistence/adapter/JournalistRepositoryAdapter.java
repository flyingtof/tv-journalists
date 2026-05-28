package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;
import org.terrevivante.tvjournalists.domain.query.SortDirection;
import org.terrevivante.tvjournalists.domain.query.SortOrder;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.specification.JournalistEntitySpecifications;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataActivityRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataJournalistRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataMediaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataThemeRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
@Transactional
public class JournalistRepositoryAdapter implements JournalistRepository {

    private final SpringDataJournalistRepository journalistRepo;
    private final SpringDataActivityRepository activityRepo;
    private final SpringDataMediaRepository mediaRepo;
    private final SpringDataThemeRepository themeRepo;
    private final PersistenceJournalistMapper mapper;

    public JournalistRepositoryAdapter(SpringDataJournalistRepository journalistRepo,
                                       SpringDataActivityRepository activityRepo,
                                       SpringDataMediaRepository mediaRepo,
                                       SpringDataThemeRepository themeRepo,
                                       PersistenceJournalistMapper mapper) {
        this.journalistRepo = journalistRepo;
        this.activityRepo = activityRepo;
        this.mediaRepo = mediaRepo;
        this.themeRepo = themeRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Journalist> search(JournalistSearchCriteria criteria, PageRequest pageRequest) {
        Specification<JournalistEntity> spec = Specification
            .where(JournalistEntitySpecifications.hasName(criteria.name()))
            .and(JournalistEntitySpecifications.hasMedia(criteria.media()))
            .and(JournalistEntitySpecifications.hasThemes(criteria.themes()));

        org.springframework.data.domain.PageRequest springPage =
            org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                toSpringSort(pageRequest.sortOrders()));

        Page<JournalistEntity> page = journalistRepo.findAll(spec, springPage);

        List<UUID> ids = page.getContent().stream().map(JournalistEntity::getId).toList();
        List<JournalistEntity> withActivities = ids.isEmpty()
            ? List.of()
            : journalistRepo.findWithActivitiesByIds(ids);

        // findWithActivitiesByIds does not preserve the sorted page order; restore it.
        if (withActivities.size() > 1) {
            Map<UUID, Integer> idToPosition = new HashMap<>();
            for (int i = 0; i < ids.size(); i++) {
                idToPosition.put(ids.get(i), i);
            }
            withActivities = withActivities.stream()
                .sorted(Comparator.comparingInt(je -> idToPosition.getOrDefault(je.getId(), Integer.MAX_VALUE)))
                .toList();
        }

        if (!withActivities.isEmpty()) {
            List<UUID> activityIds = withActivities.stream()
                .flatMap(j -> j.getActivities().stream())
                .map(ActivityEntity::getId)
                .toList();
            if (!activityIds.isEmpty()) {
                List<ActivityEntity> activitiesWithThemes = activityRepo.findWithThemesByIds(activityIds);
                mapper.attachThemes(withActivities, activitiesWithThemes);
            }
        }

        List<Journalist> content = mapper.toDomainList(withActivities);
        return new PageResult<>(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Journalist> findById(UUID id) {
        return journalistRepo.findWithActivitiesById(id).map(entity -> {
            List<UUID> activityIds = entity.getActivities().stream()
                .map(ActivityEntity::getId).toList();
            if (!activityIds.isEmpty()) {
                List<ActivityEntity> withThemes = activityRepo.findWithThemesByIds(activityIds);
                mapper.attachThemes(List.of(entity), withThemes);
            }
            return mapper.toDomain(entity);
        });
    }

    @Override
    public Journalist save(Journalist journalist) {
        JournalistEntity entity = journalist.id() == null
            ? new JournalistEntity()
            : journalistRepo.findById(journalist.id()).orElse(new JournalistEntity());
        entity.setFirstName(journalist.firstName());
        entity.setLastName(journalist.lastName());
        entity.setGlobalEmail(journalist.globalEmail());
        entity.setGlobalPhone(journalist.globalPhone());
        syncActivities(entity, journalist.activities());
        JournalistEntity saved = journalistRepo.save(entity);
        return mapper.toDomain(saved);
    }

    private void syncActivities(JournalistEntity entity, List<Activity> activities) {
        Map<UUID, ActivityEntity> existingById = entity.getActivities().stream()
            .filter(a -> a.getId() != null)
            .collect(java.util.stream.Collectors.toMap(ActivityEntity::getId,
                java.util.function.Function.identity()));

        Set<ActivityEntity> toKeep = new HashSet<>();
        List<ActivityEntity> newOrder = new ArrayList<>();

        for (Activity activity : activities) {
            ActivityEntity actEntity;
            if (activity.id() != null && existingById.containsKey(activity.id())) {
                actEntity = existingById.get(activity.id());
            } else {
                actEntity = new ActivityEntity();
                actEntity.setJournalist(entity);
            }
            actEntity.setMedia(mediaRepo.getReferenceById(activity.media().id()));
            actEntity.setRole(activity.role());
            actEntity.setSpecificEmail(activity.specificEmail());
            actEntity.setSpecificPhone(activity.specificPhone());
            actEntity.getThemes().clear();
            for (Theme theme : activity.themes()) {
                actEntity.getThemes().add(themeRepo.getReferenceById(theme.id()));
            }
            toKeep.add(actEntity);
            newOrder.add(actEntity);
        }

        entity.getActivities().removeIf(a -> !toKeep.contains(a));
        for (ActivityEntity act : newOrder) {
            if (!entity.getActivities().contains(act)) {
                entity.getActivities().add(act);
            }
        }
    }

    @Override
    public void deleteById(UUID id) {
        journalistRepo.deleteById(id);
    }

    private static Sort toSpringSort(List<SortOrder> sortOrders) {
        if (sortOrders == null || sortOrders.isEmpty()) return Sort.unsorted();
        List<Sort.Order> springOrders = sortOrders.stream().map(so -> {
            Sort.Direction dir = so.direction() == SortDirection.DESC
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
            return new Sort.Order(dir, so.field());
        }).toList();
        return Sort.by(springOrders);
    }
}
