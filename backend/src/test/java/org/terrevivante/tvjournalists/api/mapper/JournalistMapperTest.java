package org.terrevivante.tvjournalists.api.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.terrevivante.tvjournalists.api.dto.ActivityDTO;
import org.terrevivante.tvjournalists.api.dto.InteractionCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistDTO;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.model.Theme;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JournalistMapperTest {

    private final JournalistMapper mapper = Mappers.getMapper(JournalistMapper.class);

    // ── toDto(Journalist) ─────────────────────────────────────────────────────

    @Test
    void toDto_journalist_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Journalist journalist = new Journalist(id, "Alice", "Green", "alice@example.com",
                "+33600000000", now, now, List.of());

        JournalistDTO dto = mapper.toDto(journalist);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Green");
        assertThat(dto.getGlobalEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getGlobalPhone()).isEqualTo("+33600000000");
    }

    @Test
    void toDto_journalist_mapsNestedActivitiesWithThemes() {
        UUID themeId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        UUID journalistId = UUID.randomUUID();

        Theme theme = new Theme(themeId, "Biodiversity");
        Media media = new Media(mediaId, "Green Press", MediaType.PRESS, null);
        Activity activity = new Activity(activityId, journalistId, media,
                "Reporter", "act@example.com", "+33611111111", List.of(theme));

        Journalist journalist = new Journalist(journalistId, "Alice", "Green",
                null, null, null, null, List.of(activity));

        JournalistDTO dto = mapper.toDto(journalist);

        assertThat(dto.getActivities()).hasSize(1);
        ActivityDTO actDto = dto.getActivities().getFirst();
        assertThat(actDto.getId()).isEqualTo(activityId);
        assertThat(actDto.getThemes()).hasSize(1);
        ThemeDTO themeDto = actDto.getThemes().iterator().next();
        assertThat(themeDto.getId()).isEqualTo(themeId);
        assertThat(themeDto.getName()).isEqualTo("Biodiversity");
    }

    @Test
    void toDto_journalist_withNullInput_returnsNull() {
        assertThat(mapper.toDto((Journalist) null)).isNull();
    }

    // ── toDto(Activity) ───────────────────────────────────────────────────────

    @Test
    void toDto_activity_flattensMediaToIdAndName() {
        UUID mediaId = UUID.randomUUID();
        Media media = new Media(mediaId, "Le Monde", MediaType.PRESS, "https://lemonde.fr");
        Activity activity = new Activity(UUID.randomUUID(), UUID.randomUUID(), media,
                "Columnist", null, null, List.of());

        ActivityDTO dto = mapper.toDto(activity);

        assertThat(dto.getMediaId()).isEqualTo(mediaId);
        assertThat(dto.getMediaName()).isEqualTo("Le Monde");
    }

    @Test
    void toDto_activity_themesReturnedAsLinkedHashSet() {
        UUID t1 = UUID.randomUUID();
        UUID t2 = UUID.randomUUID();
        Media media = new Media(UUID.randomUUID(), "Press", MediaType.PRESS, null);
        Activity activity = new Activity(UUID.randomUUID(), UUID.randomUUID(), media, null, null, null,
                List.of(new Theme(t1, "Alpha"), new Theme(t2, "Beta")));

        ActivityDTO dto = mapper.toDto(activity);

        Set<ThemeDTO> themes = dto.getThemes();
        assertThat(themes).isInstanceOf(LinkedHashSet.class);
        List<UUID> ids = themes.stream().map(ThemeDTO::getId).toList();
        assertThat(ids).containsExactly(t1, t2);
    }

    @Test
    void toDto_activity_withNullInput_returnsNull() {
        assertThat(mapper.toDto((Activity) null)).isNull();
    }

    // ── toDto(Theme) ──────────────────────────────────────────────────────────

    @Test
    void toDto_theme_mapsIdAndName() {
        UUID id = UUID.randomUUID();
        ThemeDTO dto = mapper.toDto(new Theme(id, "Environment"));

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Environment");
    }

    @Test
    void toDto_theme_withNullInput_returnsNull() {
        assertThat(mapper.toDto((Theme) null)).isNull();
    }

    // ── toDto(InteractionLog) ─────────────────────────────────────────────────

    @Test
    void toDto_interactionLog_withNullInput_returnsNull() {
        assertThat(mapper.toDto((InteractionLog) null)).isNull();
    }

    @Test
    void toDto_interactionLog_mapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2024, 6, 1);

        InteractionLog log = new InteractionLog(id, journalistId, activityId,
                date, "Met at conference", null, null);

        var dto = mapper.toDto(log);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getDate()).isEqualTo(date);
        assertThat(dto.getDescription()).isEqualTo("Met at conference");
        assertThat(dto.getActivityId()).isEqualTo(activityId);
    }

    // ── toCommand(UUID, InteractionCreateDTO) ─────────────────────────────────

    @Test
    void toCommand_logInteraction_journalistIdComesFromUuidParam_notFromDto() {
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2024, 1, 15);

        InteractionCreateDTO dto = new InteractionCreateDTO();
        dto.setActivityId(activityId);
        dto.setDate(date);
        dto.setDescription("Interview follow-up");

        LogInteractionCommand cmd = mapper.toCommand(journalistId, dto);

        assertThat(cmd.journalistId()).isEqualTo(journalistId);
    }

    @Test
    void toCommand_logInteraction_mapsAllDtoFields() {
        UUID journalistId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        LocalDate date = LocalDate.of(2024, 3, 10);

        InteractionCreateDTO dto = new InteractionCreateDTO();
        dto.setActivityId(activityId);
        dto.setDate(date);
        dto.setDescription("Follow-up call");

        LogInteractionCommand cmd = mapper.toCommand(journalistId, dto);

        assertThat(cmd.activityId()).isEqualTo(activityId);
        assertThat(cmd.date()).isEqualTo(date);
        assertThat(cmd.description()).isEqualTo("Follow-up call");
    }

    @Test
    void toCommand_logInteraction_createdByIsNull() {
        UUID journalistId = UUID.randomUUID();
        InteractionCreateDTO dto = new InteractionCreateDTO();
        dto.setDate(LocalDate.of(2024, 1, 1));
        dto.setDescription("Note");

        LogInteractionCommand cmd = mapper.toCommand(journalistId, dto);

        assertThat(cmd.createdBy()).isNull();
    }

    @Test
    void toCommand_logInteraction_withNullDto_returnsNull() {
        assertThat(mapper.toCommand(UUID.randomUUID(), null)).isNull();
    }

    // ── toCommand(JournalistCreateDTO) ────────────────────────────────────────

    @Test
    void toCommand_createJournalist_mapsAllFields() {
        JournalistCreateDTO dto = new JournalistCreateDTO();
        dto.setFirstName("Bob");
        dto.setLastName("Smith");
        dto.setGlobalEmail("bob@example.com");
        dto.setGlobalPhone("+33622334455");

        CreateJournalistCommand cmd = mapper.toCommand(dto);

        assertThat(cmd.firstName()).isEqualTo("Bob");
        assertThat(cmd.lastName()).isEqualTo("Smith");
        assertThat(cmd.globalEmail()).isEqualTo("bob@example.com");
        assertThat(cmd.globalPhone()).isEqualTo("+33622334455");
    }

    @Test
    void toCommand_createJournalist_withNullDto_returnsNull() {
        assertThat(mapper.toCommand((JournalistCreateDTO) null)).isNull();
    }
}
