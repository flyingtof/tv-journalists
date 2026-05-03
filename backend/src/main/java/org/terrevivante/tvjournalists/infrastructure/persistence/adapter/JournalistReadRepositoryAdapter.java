package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.application.port.JournalistReadRepository;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;
import org.terrevivante.tvjournalists.domain.query.SortDirection;
import org.terrevivante.tvjournalists.domain.query.SortOrder;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistReadMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.specification.JournalistEntitySpecifications;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataActivityRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataJournalistRepository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional(readOnly = true)
public class JournalistReadRepositoryAdapter implements JournalistReadRepository {

    private final SpringDataJournalistRepository journalistRepo;
    private final SpringDataActivityRepository activityRepo;
    private final PersistenceJournalistReadMapper readMapper;

    public JournalistReadRepositoryAdapter(SpringDataJournalistRepository journalistRepo,
                                           SpringDataActivityRepository activityRepo,
                                           PersistenceJournalistReadMapper readMapper) {
        this.journalistRepo = journalistRepo;
        this.activityRepo = activityRepo;
        this.readMapper = readMapper;
    }

    @Override
    public PageResult<JournalistListItemView> search(JournalistSearchCriteria criteria, PageRequest pageRequest) {
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
                readMapper.attachThemes(withActivities, activitiesWithThemes);
            }
        }

        List<JournalistListItemView> content = readMapper.toListItemViews(withActivities);
        return new PageResult<>(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public Optional<JournalistProfileView> findProfileById(UUID id) {
        return journalistRepo.findWithActivitiesById(id).map(entity -> {
            List<UUID> activityIds = entity.getActivities().stream().map(ActivityEntity::getId).toList();
            if (!activityIds.isEmpty()) {
                List<ActivityEntity> withThemes = activityRepo.findWithThemesByIds(activityIds);
                readMapper.attachThemes(List.of(entity), withThemes);
            }
            return readMapper.toProfileView(entity);
        });
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

