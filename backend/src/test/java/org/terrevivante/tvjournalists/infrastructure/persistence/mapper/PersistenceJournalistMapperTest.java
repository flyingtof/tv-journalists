package org.terrevivante.tvjournalists.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.InteractionLogEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PersistenceJournalistMapperTest {

    private final PersistenceJournalistMapper mapper = new PersistenceJournalistMapperImpl();

    // ── toDomain(JournalistEntity) ────────────────────────────────────────────

    @Test
    void toDomain_journalist_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.now().minusDays(1);
        OffsetDateTime updatedAt = OffsetDateTime.now();

        JournalistEntity entity = new JournalistEntity("Alice", "Green");
        entity.setId(id);
        entity.setGlobalEmail("alice@example.com");
        entity.setGlobalPhone("+33600000000");
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        MediaEntity media = mediaEntity("Green Press");
        ActivityEntity activity = activityEntity(entity, media);
        entity.getActivities().add(activity);

        Journalist domain = mapper.toDomain(entity);

        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.firstName()).isEqualTo("Alice");
        assertThat(domain.lastName()).isEqualTo("Green");
        assertThat(domain.globalEmail()).isEqualTo("alice@example.com");
        assertThat(domain.globalPhone()).isEqualTo("+33600000000");
        assertThat(domain.createdAt()).isEqualTo(createdAt);
        assertThat(domain.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    void toDomain_journalist_activitiesAreMapped() {
        JournalistEntity entity = new JournalistEntity("Bob", "Smith");
        entity.setId(UUID.randomUUID());

        MediaEntity media = mediaEntity("Daily News");
        ActivityEntity activity = activityEntity(entity, media);
        entity.getActivities().add(activity);

        Journalist domain = mapper.toDomain(entity);

        assertThat(domain.activities()).hasSize(1);
        assertThat(domain.activities().getFirst().media().name()).isEqualTo("Daily News");
    }

    @Test
    void toDomain_journalist_withNullActivities_returnsEmptyActivities() {
        JournalistEntity entity = new JournalistEntity("Bob", "Smith");
        entity.setId(UUID.randomUUID());
        entity.setActivities(null);

        Journalist domain = mapper.toDomain(entity);

        assertThat(domain.activities()).isEmpty();
    }

    // ── toDomain(ActivityEntity) ──────────────────────────────────────────────

    @Test
    void toDomain_activity_mapsMediaFields() {
        UUID mediaId = UUID.randomUUID();
        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setId(mediaId);
        mediaEntity.setName("Le Figaro");
        mediaEntity.setType(MediaType.PRESS);
        mediaEntity.setUrl("https://lefigaro.fr");

        ActivityEntity entity = new ActivityEntity();
        entity.setId(UUID.randomUUID());
        entity.setMedia(mediaEntity);
        entity.setRole("Columnist");
        entity.setSpecificEmail("col@figaro.fr");
        entity.setSpecificPhone("+33612345678");

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.media().id()).isEqualTo(mediaId);
        assertThat(domain.media().name()).isEqualTo("Le Figaro");
        assertThat(domain.media().type()).isEqualTo(MediaType.PRESS);
        assertThat(domain.media().url()).isEqualTo("https://lefigaro.fr");
        assertThat(domain.role()).isEqualTo("Columnist");
        assertThat(domain.specificEmail()).isEqualTo("col@figaro.fr");
        assertThat(domain.specificPhone()).isEqualTo("+33612345678");
    }

    @Test
    void toDomain_activity_mapsThemesList() {
        UUID themeId = UUID.randomUUID();
        ThemeEntity themeEntity = new ThemeEntity();
        themeEntity.setId(themeId);
        themeEntity.setName("Climate");

        ActivityEntity entity = new ActivityEntity();
        entity.setId(UUID.randomUUID());
        entity.setMedia(mediaEntity("EcoPress"));
        entity.getThemes().add(themeEntity);

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.themes()).hasSize(1);
        Theme theme = domain.themes().getFirst();
        assertThat(theme.id()).isEqualTo(themeId);
        assertThat(theme.name()).isEqualTo("Climate");
    }

    @Test
    void toDomain_activity_withNullThemes_returnsEmptyThemes() {
        ActivityEntity entity = new ActivityEntity();
        entity.setId(UUID.randomUUID());
        entity.setMedia(mediaEntity("EcoPress"));
        entity.setThemes(null);

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.themes()).isEmpty();
    }

    @Test
    void toDomain_activity_journalistIdTakenFromJournalistAssociation() {
        UUID journalistId = UUID.randomUUID();
        JournalistEntity journalist = new JournalistEntity("Alice", "Green");
        journalist.setId(journalistId);

        ActivityEntity entity = activityEntity(journalist, mediaEntity("Press"));

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.journalistId()).isEqualTo(journalistId);
    }

    @Test
    void toDomain_activity_withNoJournalist_journalistIdIsNull() {
        ActivityEntity entity = new ActivityEntity();
        entity.setId(UUID.randomUUID());
        entity.setMedia(mediaEntity("Press"));

        Activity domain = mapper.toDomain(entity);

        assertThat(domain.journalistId()).isNull();
    }

    // ── toDomain(InteractionLogEntity) ────────────────────────────────────────

    @Test
    void toDomain_interactionLog_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDate date = LocalDate.of(2024, 5, 20);
        OffsetDateTime createdAt = OffsetDateTime.now();

        InteractionLogEntity entity = new InteractionLogEntity();
        entity.setId(id);
        entity.setJournalistId(journalistId);
        entity.setActivityId(activityId);
        entity.setDate(date);
        entity.setDescription("Met at summit");
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(createdAt);

        InteractionLog domain = mapper.toDomain(entity);

        assertThat(domain.id()).isEqualTo(id);
        assertThat(domain.journalistId()).isEqualTo(journalistId);
        assertThat(domain.activityId()).isEqualTo(activityId);
        assertThat(domain.date()).isEqualTo(date);
        assertThat(domain.description()).isEqualTo("Met at summit");
        assertThat(domain.createdBy()).isEqualTo(createdBy);
        assertThat(domain.createdAt()).isEqualTo(createdAt);
    }

    // ── toEntity(InteractionLog) ──────────────────────────────────────────────

    @Test
    void toEntity_interactionLog_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID createdBy = UUID.randomUUID();
        LocalDate date = LocalDate.of(2024, 7, 4);
        OffsetDateTime createdAt = OffsetDateTime.now();

        InteractionLog domain = new InteractionLog(id, journalistId, activityId,
                date, "Press briefing", createdBy, createdAt);

        InteractionLogEntity entity = mapper.toEntity(domain);

        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getJournalistId()).isEqualTo(journalistId);
        assertThat(entity.getActivityId()).isEqualTo(activityId);
        assertThat(entity.getDate()).isEqualTo(date);
        assertThat(entity.getDescription()).isEqualTo("Press briefing");
        assertThat(entity.getCreatedBy()).isEqualTo(createdBy);
        assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
    }

    // ── toDomainList(List<JournalistEntity>) ──────────────────────────────────

    @Test
    void toDomainList_emptyList_returnsEmptyList() {
        assertThat(mapper.toDomainList(List.of())).isEmpty();
    }

    @Test
    void toDomainList_multipleEntities_mapsEachToDomain() {
        JournalistEntity e1 = new JournalistEntity("Alice", "Green");
        e1.setId(UUID.randomUUID());
        e1.getActivities().add(activityEntity(e1, mediaEntity("Press A")));

        JournalistEntity e2 = new JournalistEntity("Bob", "Smith");
        e2.setId(UUID.randomUUID());
        e2.getActivities().add(activityEntity(e2, mediaEntity("Press B")));

        List<Journalist> result = mapper.toDomainList(List.of(e1, e2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).firstName()).isEqualTo("Alice");
        assertThat(result.get(1).firstName()).isEqualTo("Bob");
    }

    // ── attachThemes(List<JournalistEntity>, List<ActivityEntity>) ────────────

    @Test
    void attachThemes_populatesThemesFromSeparateActivityList() {
        ThemeEntity theme = new ThemeEntity();
        theme.setId(UUID.randomUUID());
        theme.setName("Climate");

        JournalistEntity journalist = new JournalistEntity("Alice", "Green");
        journalist.setId(UUID.randomUUID());
        ActivityEntity sparse = activityEntity(journalist, mediaEntity("Press"));
        journalist.getActivities().add(sparse);

        // separate activity bearing the themes
        ActivityEntity withThemes = new ActivityEntity();
        withThemes.setId(sparse.getId());
        withThemes.getThemes().add(theme);

        mapper.attachThemes(List.of(journalist), List.of(withThemes));

        assertThat(journalist.getActivities().getFirst().getThemes())
            .hasSize(1)
            .extracting(ThemeEntity::getName)
            .containsExactly("Climate");
    }

    @Test
    void attachThemes_activityMissingFromThemeList_themesUnchanged() {
        JournalistEntity journalist = new JournalistEntity("Bob", "Smith");
        journalist.setId(UUID.randomUUID());
        // no themes pre-populated: matches real production precondition (activities are sparse)
        ActivityEntity activity = activityEntity(journalist, mediaEntity("Press"));
        journalist.getActivities().add(activity);

        // themes list does not contain this activity → null guard must fire
        mapper.attachThemes(List.of(journalist), List.of());

        assertThat(journalist.getActivities().getFirst().getThemes()).isEmpty();
    }

    @Test
    void attachThemes_activityFoundWithEmptyThemes_replacesWithEmptyCollection() {
        ThemeEntity staleTheme = new ThemeEntity();
        staleTheme.setId(UUID.randomUUID());
        staleTheme.setName("Stale");

        JournalistEntity journalist = new JournalistEntity("Alice", "Green");
        journalist.setId(UUID.randomUUID());
        ActivityEntity sparse = activityEntity(journalist, mediaEntity("Press"));
        sparse.getThemes().add(staleTheme);
        journalist.getActivities().add(sparse);

        // matching activity IS in the list but carries no themes
        ActivityEntity withNoThemes = new ActivityEntity();
        withNoThemes.setId(sparse.getId());

        mapper.attachThemes(List.of(journalist), List.of(withNoThemes));

        assertThat(journalist.getActivities().getFirst().getThemes()).isEmpty();
    }

    @Test
    void attachThemes_multipleJournalistsAndActivities_eachReceivesCorrectThemes() {
        ThemeEntity themeA = new ThemeEntity();
        themeA.setId(UUID.randomUUID());
        themeA.setName("Politics");

        ThemeEntity themeB = new ThemeEntity();
        themeB.setId(UUID.randomUUID());
        themeB.setName("Sport");

        JournalistEntity j1 = new JournalistEntity("Alice", "Green");
        j1.setId(UUID.randomUUID());
        ActivityEntity a1 = activityEntity(j1, mediaEntity("Press A"));
        j1.getActivities().add(a1);

        JournalistEntity j2 = new JournalistEntity("Bob", "Smith");
        j2.setId(UUID.randomUUID());
        ActivityEntity a2 = activityEntity(j2, mediaEntity("Press B"));
        j2.getActivities().add(a2);

        ActivityEntity withThemesA = new ActivityEntity();
        withThemesA.setId(a1.getId());
        withThemesA.getThemes().add(themeA);

        ActivityEntity withThemesB = new ActivityEntity();
        withThemesB.setId(a2.getId());
        withThemesB.getThemes().add(themeB);

        mapper.attachThemes(List.of(j1, j2), List.of(withThemesA, withThemesB));

        assertThat(j1.getActivities().getFirst().getThemes())
            .extracting(ThemeEntity::getName).containsExactly("Politics");
        assertThat(j2.getActivities().getFirst().getThemes())
            .extracting(ThemeEntity::getName).containsExactly("Sport");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private MediaEntity mediaEntity(String name) {
        MediaEntity media = new MediaEntity();
        media.setId(UUID.randomUUID());
        media.setName(name);
        media.setType(MediaType.PRESS);
        return media;
    }

    private ActivityEntity activityEntity(JournalistEntity journalist, MediaEntity media) {
        ActivityEntity activity = new ActivityEntity();
        activity.setId(UUID.randomUUID());
        activity.setJournalist(journalist);
        activity.setMedia(media);
        return activity;
    }
}
