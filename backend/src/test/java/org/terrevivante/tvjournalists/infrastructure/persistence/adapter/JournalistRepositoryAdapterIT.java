package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.api.JournalistFixtures;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;

import org.terrevivante.tvjournalists.infrastructure.persistence.entity.InteractionLogEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JournalistRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private JournalistRepository journalistRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var biodiversity = fixtures.persistTheme("Biodiversity");
        var greenPress = fixtures.persistMedia("Green Press");
        fixtures.persistJournalistWithActivity("Alice", "Green", greenPress, biodiversity);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnCoreJournalistsFromJpaAdapterSearch() {
        PageResult<Journalist> result = journalistRepository.search(
            new JournalistSearchCriteria("Alice", List.of("Green Press"), List.of("Biodiversity")),
            new PageRequest(0, 20)
        );

        assertThat(result.content()).extracting(Journalist::firstName).contains("Alice");
        assertThat(result.content().getFirst().activities()).isNotEmpty();
    }

    @Test
    void findById_mergesActivitiesSharingTheSameMediaAndCombinesThemes() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        String suffix = UUID.randomUUID().toString();
        String financeVerteName = "Finance Verte " + suffix;
        String deforestationName = "Déforestation " + suffix;
        String lciName = "LCI " + suffix;
        var financeVerte = fixtures.persistTheme(financeVerteName);
        var deforestation = fixtures.persistTheme(deforestationName);
        var lci = fixtures.persistMedia(lciName);
        JournalistEntity journalist = fixtures.persistJournalist("Nicolas", "Andre");

        persistActivity(journalist, lci, financeVerte);
        persistActivity(journalist, lci, deforestation);
        entityManager.flush();
        entityManager.clear();

        Journalist result = journalistRepository.findById(journalist.getId()).orElseThrow();

        assertThat(result.activities()).hasSize(1);
        Activity activity = result.activities().getFirst();
        assertThat(activity.media().name()).isEqualTo(lciName);
        assertThat(activity.themes())
            .extracting(Theme::name)
            .containsExactlyInAnyOrder(financeVerteName, deforestationName);
    }

    @Test
    void search_mergesActivitiesSharingTheSameMediaAndCombinesThemes() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        String suffix = UUID.randomUUID().toString();
        String financeVerteName = "Finance Verte " + suffix;
        String deforestationName = "Déforestation " + suffix;
        String lciName = "LCI " + suffix;
        var financeVerte = fixtures.persistTheme(financeVerteName);
        var deforestation = fixtures.persistTheme(deforestationName);
        var lci = fixtures.persistMedia(lciName);
        JournalistEntity journalist = fixtures.persistJournalist("Nicolas", "Andre");

        persistActivity(journalist, lci, financeVerte);
        persistActivity(journalist, lci, deforestation);
        entityManager.flush();
        entityManager.clear();

        PageResult<Journalist> result = journalistRepository.search(
            new JournalistSearchCriteria("Nicolas", List.of(lciName), List.of()),
            new PageRequest(0, 20)
        );

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().activities()).hasSize(1);
        assertThat(result.content().getFirst().activities().getFirst().themes())
            .extracting(Theme::name)
            .containsExactlyInAnyOrder(financeVerteName, deforestationName);
    }

    @Test
    void save_persistsActivitiesAndThemes() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var climate = fixtures.persistTheme("Climate");
        var rtMedia = fixtures.persistMedia("RT");
        entityManager.flush();
        entityManager.clear();

        Activity activity = new Activity(
            null,
            null,
            new org.terrevivante.tvjournalists.domain.model.Media(rtMedia.getId(), rtMedia.getName(), rtMedia.getType(), null),
            "Reporter",
            null,
            null,
            List.of(new Theme(climate.getId(), climate.getName()))
        );
        Journalist toSave = new Journalist(null, "Jean", "Dupont", null, null, null, null, List.of(activity));

        Journalist saved = journalistRepository.save(toSave);
        entityManager.flush();
        entityManager.clear();

        Journalist reloaded = journalistRepository.findById(saved.id()).orElseThrow();

        assertThat(reloaded.activities()).hasSize(1);
        assertThat(reloaded.activities().getFirst().media().name()).isEqualTo("RT");
        assertThat(reloaded.activities().getFirst().role()).isEqualTo("Reporter");
        assertThat(reloaded.activities().getFirst().themes())
            .extracting(Theme::name)
            .containsExactly("Climate");
    }

    @Test
    void save_updatePreservesExistingActivityIdAndInteractionReference() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var climate = fixtures.persistTheme("Climate " + UUID.randomUUID());
        var rtMedia = fixtures.persistMedia("RT " + UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();

        // 1. Create journalist with one activity
        var domainMedia = new org.terrevivante.tvjournalists.domain.model.Media(
            rtMedia.getId(), rtMedia.getName(), rtMedia.getType(), null);
        Activity activity = new Activity(null, null, domainMedia, "Reporter", null, null,
            List.of(new Theme(climate.getId(), climate.getName())));
        Journalist created = journalistRepository.save(
            new Journalist(null, "Jean", "Dupont", null, null, null, null, List.of(activity)));
        entityManager.flush();
        entityManager.clear();

        UUID originalActivityId = created.activities().getFirst().id();
        assertThat(originalActivityId).isNotNull();

        // 2. Create an interaction log linked to that activity
        InteractionLogEntity log = new InteractionLogEntity();
        log.setJournalistId(created.id());
        log.setActivityId(originalActivityId);
        log.setDate(LocalDate.now());
        log.setDescription("Test interaction");
        entityManager.persist(log);
        entityManager.flush();
        entityManager.clear();

        // 3. Update journalist while preserving the existing activity id
        Activity preservedActivity = new Activity(
            originalActivityId, created.id(), domainMedia, "Presenter", null, null, List.of());
        journalistRepository.save(
            new Journalist(created.id(), "Jean", "Martin", null, null, null, null, List.of(preservedActivity)));
        entityManager.flush();
        entityManager.clear();

        // 4. Verify activity id still exists and interaction log is still valid
        Journalist reloaded = journalistRepository.findById(created.id()).orElseThrow();
        assertThat(reloaded.activities()).hasSize(1);
        assertThat(reloaded.activities().getFirst().id()).isEqualTo(originalActivityId);

        InteractionLogEntity reloadedLog = entityManager.find(InteractionLogEntity.class, log.getId());
        assertThat(reloadedLog).isNotNull();
        assertThat(reloadedLog.getActivityId()).isEqualTo(originalActivityId);
    }

    private void persistActivity(JournalistEntity journalist,
                                 MediaEntity media,
                                 ThemeEntity... themes) {
        ActivityEntity activity = new ActivityEntity();
        activity.setJournalist(journalist);
        activity.setMedia(media);
        for (var theme : themes) {
            activity.getThemes().add(theme);
        }
        journalist.getActivities().add(activity);
        entityManager.persist(activity);
    }
}
