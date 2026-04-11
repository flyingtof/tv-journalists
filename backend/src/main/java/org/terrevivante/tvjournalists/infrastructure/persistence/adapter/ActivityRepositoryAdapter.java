package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.mapper.PersistenceJournalistMapper;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataActivityRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataJournalistRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataMediaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataThemeRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public class ActivityRepositoryAdapter implements ActivityRepository {

    private final SpringDataActivityRepository activityRepo;
    private final SpringDataJournalistRepository journalistRepo;
    private final SpringDataMediaRepository mediaRepo;
    private final SpringDataThemeRepository themeRepo;
    private final PersistenceJournalistMapper mapper;

    public ActivityRepositoryAdapter(SpringDataActivityRepository activityRepo,
                                     SpringDataJournalistRepository journalistRepo,
                                     SpringDataMediaRepository mediaRepo,
                                     SpringDataThemeRepository themeRepo,
                                     PersistenceJournalistMapper mapper) {
        this.activityRepo = activityRepo;
        this.journalistRepo = journalistRepo;
        this.mediaRepo = mediaRepo;
        this.themeRepo = themeRepo;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Activity> findById(UUID id) {
        return activityRepo.findById(id).map(entity -> {
            List<ActivityEntity> withThemes = activityRepo.findWithThemesByIds(List.of(entity.getId()));
            ActivityEntity resolved = withThemes.isEmpty() ? entity : withThemes.get(0);
            return mapper.toDomain(resolved);
        });
    }

    @Override
    public Activity save(Activity activity) {
        ActivityEntity entity = activity.id() == null
            ? new ActivityEntity()
            : activityRepo.findById(activity.id()).orElse(new ActivityEntity());
        JournalistEntity journalist = journalistRepo.findById(activity.journalistId())
            .orElseThrow(() -> new IllegalArgumentException("Journalist not found: " + activity.journalistId()));
        MediaEntity media = mediaRepo.findById(activity.media().id())
            .orElseThrow(() -> new IllegalArgumentException("Media not found: " + activity.media().id()));
        entity.setJournalist(journalist);
        entity.setMedia(media);
        entity.setRole(activity.role());
        entity.setSpecificEmail(activity.specificEmail());
        entity.setSpecificPhone(activity.specificPhone());
        List<ThemeEntity> themes = activity.themes().stream()
            .map(t -> themeRepo.findById(t.id())
                .orElseThrow(() -> new IllegalArgumentException("Theme not found: " + t.id())))
            .toList();
        entity.getThemes().clear();
        entity.getThemes().addAll(themes);
        return mapper.toDomain(activityRepo.save(entity));
    }
}
