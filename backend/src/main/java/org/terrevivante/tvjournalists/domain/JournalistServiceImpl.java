package org.terrevivante.tvjournalists.domain;

import org.terrevivante.tvjournalists.persistence.JournalistRepository;
import org.terrevivante.tvjournalists.persistence.ActivityRepository;
import org.terrevivante.tvjournalists.persistence.specification.JournalistSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.terrevivante.tvjournalists.domain.Activity;

@Service
@Transactional
public class JournalistServiceImpl implements JournalistService {

    private final JournalistRepository journalistRepository;
    private final ActivityRepository activityRepository;

    public JournalistServiceImpl(JournalistRepository journalistRepository, ActivityRepository activityRepository) {
        this.journalistRepository = journalistRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    public Journalist save(Journalist journalist) {
        return journalistRepository.save(journalist);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Journalist> findById(UUID id) {
        // Load journalist with activities, media and themes to avoid N+1 selects when rendering details
        return journalistRepository.findWithActivitiesById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Journalist> search(String name, List<String> media, List<String> themes, Pageable pageable) {
        Specification<Journalist> spec = Specification.where(JournalistSpecifications.hasName(name))
            .and(JournalistSpecifications.hasMedia(media))
            .and(JournalistSpecifications.hasThemes(themes));
        Page<Journalist> page = journalistRepository.findAll(spec, pageable);

        // To avoid the Hibernate warning about applying pagination in-memory when fetching collection associations,
        // we load collections separately for the page elements.
        List<UUID> ids = page.stream().map(Journalist::getId).toList();
        if (!ids.isEmpty()) {
            List<Journalist> withActivities = journalistRepository.findWithActivitiesByIds(ids);
            // Map loaded activities back to the page entities
            Map<UUID, List<Activity>> activitiesByJournalist = withActivities.stream()
                .collect(Collectors.toMap(Journalist::getId, Journalist::getActivities));

            // Attach activities (with media) to page entities
            page.forEach(j -> j.setActivities(activitiesByJournalist.getOrDefault(j.getId(), List.of())));

            // Collect all activity ids to fetch their themes in batch and avoid N+1
            List<UUID> activityIds = activitiesByJournalist.values().stream()
                .flatMap(List::stream)
                .map(Activity::getId)
                .collect(Collectors.toList());

            if (!activityIds.isEmpty()) {
                List<Activity> activitiesWithThemes = activityRepository.findWithThemesByIds(activityIds);
                // Map activityId -> themes
                Map<UUID, Set<org.terrevivante.tvjournalists.domain.Theme>> activityThemesByActivity = activitiesWithThemes.stream()
                    .collect(Collectors.toMap(Activity::getId, Activity::getThemes));

                // Attach themes to the activities present in the page entities
                activitiesByJournalist.values().forEach(list -> {
                    list.forEach(a -> {
                        Set<org.terrevivante.tvjournalists.domain.Theme> activityThemes = activityThemesByActivity.get(a.getId());
                        if (activityThemes != null) {
                            a.setThemes(activityThemes);
                        }
                    });
                });
            }
        }

        return page;
    }
}
