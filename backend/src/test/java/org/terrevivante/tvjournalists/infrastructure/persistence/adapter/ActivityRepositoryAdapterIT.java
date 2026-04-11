package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.api.JournalistFixtures;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ActivityRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_persistsThemeAssociations() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        ThemeEntity themeEntity = fixtures.persistTheme("Climate");
        MediaEntity mediaEntity = fixtures.persistMedia("Earth Today", MediaType.TV);
        JournalistEntity journalistEntity = fixtures.persistJournalist("Carol", "Forest");
        entityManager.flush();

        Theme theme = new Theme(themeEntity.getId(), themeEntity.getName());
        Media media = new Media(mediaEntity.getId(), mediaEntity.getName(), mediaEntity.getType(), null);
        Activity activity = new Activity(null, journalistEntity.getId(), media,
                "Journalist", null, null, List.of(theme));

        Activity saved = activityRepository.save(activity);
        entityManager.flush();
        entityManager.clear();

        Optional<Activity> reloaded = activityRepository.findById(saved.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().themes())
                .extracting(Theme::name)
                .containsExactly("Climate");
    }

    @Test
    void save_replacesThemeAssociationsOnUpdate() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        ThemeEntity climate = fixtures.persistTheme("Climate2");
        ThemeEntity ocean = fixtures.persistTheme("Ocean");
        MediaEntity mediaEntity = fixtures.persistMedia("Water Weekly", MediaType.RADIO);
        JournalistEntity journalistEntity = fixtures.persistJournalist("Dave", "Wave");
        entityManager.flush();

        Media media = new Media(mediaEntity.getId(), mediaEntity.getName(), mediaEntity.getType(), null);
        Activity original = new Activity(null, journalistEntity.getId(), media,
                "Reporter", null, null,
                List.of(new Theme(climate.getId(), climate.getName())));
        Activity saved = activityRepository.save(original);
        entityManager.flush();
        entityManager.clear();

        Activity updated = new Activity(saved.id(), journalistEntity.getId(), media,
                "Reporter", null, null,
                List.of(new Theme(ocean.getId(), ocean.getName())));
        activityRepository.save(updated);
        entityManager.flush();
        entityManager.clear();

        Optional<Activity> reloaded = activityRepository.findById(saved.id());
        assertThat(reloaded).isPresent();
        assertThat(reloaded.get().themes())
                .extracting(Theme::name)
                .containsExactly("Ocean");
    }
}
