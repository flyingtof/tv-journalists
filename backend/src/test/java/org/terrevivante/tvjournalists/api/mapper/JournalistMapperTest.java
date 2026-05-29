package org.terrevivante.tvjournalists.api.mapper;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.api.dto.InteractionCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistActivityUpsertDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistListItemDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistProfileActivityDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistProfileDTO;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.application.readmodel.ActivityView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.application.readmodel.MediaView;
import org.terrevivante.tvjournalists.application.readmodel.ThemeView;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.model.Theme;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JournalistMapperTest {

    private final JournalistMapper mapper = new  JournalistMapperImpl();

    // ── toProfileDto(Journalist) ─────────────────────────────────────────────

    @Test
    void toProfileDto_journalist_mapsAllScalarFields() {
        UUID id = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        Journalist journalist = new Journalist(id, "Alice", "Green", "alice@example.com",
                "+33600000000", now, now, List.of());

        JournalistProfileDTO dto = mapper.toProfileDto(journalist);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.firstName()).isEqualTo("Alice");
        assertThat(dto.lastName()).isEqualTo("Green");
        assertThat(dto.globalEmail()).isEqualTo("alice@example.com");
        assertThat(dto.globalPhone()).isEqualTo("+33600000000");
    }

    @Test
    void toProfileDto_journalist_mapsNestedActivitiesWithThemes() {
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

        JournalistProfileDTO dto = mapper.toProfileDto(journalist);

        assertThat(dto.activities()).hasSize(1);
        JournalistProfileActivityDTO actDto = dto.activities().getFirst();
        assertThat(actDto.id()).isEqualTo(activityId);
        assertThat(actDto.themes()).hasSize(1);
        ThemeDTO themeDto = actDto.themes().iterator().next();
        assertThat(themeDto.id()).isEqualTo(themeId);
        assertThat(themeDto.name()).isEqualTo("Biodiversity");
    }

    @Test
    void toProfileDto_journalist_withNullInput_returnsNull() {
        assertThat(mapper.toProfileDto((Journalist) null)).isNull();
    }

    @Test
    void toListItemDto_journalistListItemView_mapsOnlyFieldsNeededByList() {
        UUID journalistId = UUID.randomUUID();

        JournalistListItemView journalist = new JournalistListItemView(
            journalistId,
            "Alice",
            "Green",
            "alice@example.com",
            List.of("Green Press", "LCI")
        );

        JournalistListItemDTO dto = mapper.toListItemDto(journalist);

        assertThat(dto.id()).isEqualTo(journalistId);
        assertThat(dto.firstName()).isEqualTo("Alice");
        assertThat(dto.lastName()).isEqualTo("Green");
        assertThat(dto.globalEmail()).isEqualTo("alice@example.com");
        assertThat(dto.mediaNames()).containsExactly("Green Press", "LCI");
    }

    @Test
    void toProfileDto_profileView_mapsDetailedActivities() {
        UUID themeId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();
        JournalistProfileView profile = new JournalistProfileView(
            UUID.randomUUID(),
            "Alice",
            "Green",
            "alice@example.com",
            "+33600000000",
            List.of(new ActivityView(
                activityId,
                new MediaView(UUID.randomUUID(), "Green Press"),
                "Reporter",
                "alice.green@press.com",
                "+33611111111",
                List.of(new ThemeView(themeId, "Biodiversity"))
            ))
        );

        JournalistProfileDTO dto = mapper.toProfileDto(profile);

        assertThat(dto.activities()).hasSize(1);
        JournalistProfileActivityDTO activity = dto.activities().getFirst();
        assertThat(activity.mediaName()).isEqualTo("Green Press");
        assertThat(activity.specificEmail()).isEqualTo("alice.green@press.com");
        assertThat(activity.themes()).extracting(ThemeDTO::name).containsExactly("Biodiversity");
    }

    // ── toThemeDto(Theme) ─────────────────────────────────────────────────────

    @Test
    void toThemeDto_theme_mapsIdAndName() {
        UUID id = UUID.randomUUID();
        ThemeDTO dto = mapper.toThemeDto(new Theme(id, "Environment"));

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo("Environment");
    }

    @Test
    void toThemeDto_theme_withNullInput_returnsNull() {
        assertThat(mapper.toThemeDto((Theme) null)).isNull();
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

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.date()).isEqualTo(date);
        assertThat(dto.description()).isEqualTo("Met at conference");
        assertThat(dto.activityId()).isEqualTo(activityId);
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
        assertThat(mapper.toCommand(UUID.randomUUID(), (InteractionCreateDTO) null)).isNull();
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
    void toCommand_createJournalist_mapsActivities() {
        UUID mediaId = UUID.randomUUID();
        UUID themeId = UUID.randomUUID();

        JournalistActivityUpsertDTO activity = new JournalistActivityUpsertDTO();
        activity.setMediaId(mediaId);
        activity.setSpecificEmail("alice.green@press.com");
        activity.setSpecificPhone("+33611111111");
        activity.setThemeIds(List.of(themeId));

        JournalistCreateDTO dto = new JournalistCreateDTO();
        dto.setFirstName("Bob");
        dto.setLastName("Smith");
        dto.setActivities(List.of(activity));

        CreateJournalistCommand cmd = mapper.toCommand(dto);

        assertThat(cmd.activities()).hasSize(1);
        assertThat(cmd.activities().getFirst().mediaId()).isEqualTo(mediaId);
        assertThat(cmd.activities().getFirst().themeIds()).containsExactly(themeId);
    }

    @Test
    void toCommand_createJournalist_withNullDto_returnsNull() {
        assertThat(mapper.toCommand((JournalistCreateDTO) null)).isNull();
    }
}
