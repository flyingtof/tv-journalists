package org.terrevivante.tvjournalists.api;

import jakarta.persistence.EntityManager;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;
import org.terrevivante.tvjournalists.domain.model.MediaType;

/**
 * Fixture helper that centralises persistence of test data for journalist search tests.
 *
 * <p>All direct knowledge of how domain objects are constructed and wired together lives
 * here. When the hexagonal split introduces separate persistence entities
 * (ThemeEntity / MediaEntity / JournalistEntity), only this class needs updating;
 * the IT tests remain untouched.
 */
public class JournalistFixtures {

    private final EntityManager entityManager;

    public JournalistFixtures(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ThemeEntity persistTheme(String name) {
        ThemeEntity theme = new ThemeEntity();
        theme.setName(name);
        entityManager.persist(theme);
        return theme;
    }

    public MediaEntity persistMedia(String name) {
        return persistMedia(name, MediaType.PRESS);
    }

    public MediaEntity persistMedia(String name, MediaType type) {
        MediaEntity media = new MediaEntity();
        media.setName(name);
        media.setType(type);
        entityManager.persist(media);
        return media;
    }

    public JournalistEntity persistJournalist(String firstName, String lastName) {
        JournalistEntity journalist = new JournalistEntity(firstName, lastName);
        entityManager.persist(journalist);
        return journalist;
    }

    public JournalistEntity persistJournalistWithActivity(String firstName, String lastName,
                                                          MediaEntity media, ThemeEntity... themes) {
        JournalistEntity journalist = new JournalistEntity(firstName, lastName);
        entityManager.persist(journalist);

        ActivityEntity activity = new ActivityEntity();
        activity.setJournalist(journalist);
        activity.setMedia(media);
        for (ThemeEntity theme : themes) {
            activity.getThemes().add(theme);
        }
        journalist.getActivities().add(activity);
        entityManager.persist(activity);

        return journalist;
    }
}
