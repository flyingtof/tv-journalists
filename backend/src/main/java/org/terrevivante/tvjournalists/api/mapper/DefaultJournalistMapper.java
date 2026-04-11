package org.terrevivante.tvjournalists.api.mapper;

import org.springframework.stereotype.Component;
import org.terrevivante.tvjournalists.api.dto.*;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.domain.model.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class DefaultJournalistMapper implements JournalistMapper {

    @Override
    public JournalistDTO toDto(Journalist journalist) {
        if (journalist == null) return null;
        JournalistDTO dto = new JournalistDTO();
        dto.setId(journalist.id());
        dto.setFirstName(journalist.firstName());
        dto.setLastName(journalist.lastName());
        dto.setGlobalEmail(journalist.globalEmail());
        dto.setGlobalPhone(journalist.globalPhone());
        dto.setActivities(journalist.activities().stream().map(this::toDto).toList());
        return dto;
    }

    @Override
    public ActivityDTO toDto(Activity activity) {
        if (activity == null) return null;
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.id());
        dto.setMediaId(activity.media() != null ? activity.media().id() : null);
        dto.setMediaName(activity.media() != null ? activity.media().name() : null);
        dto.setRole(activity.role());
        dto.setSpecificEmail(activity.specificEmail());
        dto.setSpecificPhone(activity.specificPhone());
        dto.setThemes(toThemeDTOSet(activity.themes()));
        return dto;
    }

    @Override
    public ThemeDTO toDto(Theme theme) {
        if (theme == null) return null;
        ThemeDTO dto = new ThemeDTO();
        dto.setId(theme.id());
        dto.setName(theme.name());
        return dto;
    }

    @Override
    public InteractionDTO toDto(InteractionLog log) {
        if (log == null) return null;
        InteractionDTO dto = new InteractionDTO();
        dto.setId(log.id());
        dto.setDate(log.date());
        dto.setDescription(log.description());
        dto.setActivityId(log.activityId());
        return dto;
    }

    @Override
    public CreateJournalistCommand toCommand(JournalistCreateDTO dto) {
        if (dto == null) return null;
        return new CreateJournalistCommand(
            dto.getFirstName(),
            dto.getLastName(),
            dto.getGlobalEmail(),
            dto.getGlobalPhone()
        );
    }

    @Override
    public LogInteractionCommand toCommand(UUID journalistId, InteractionCreateDTO dto) {
        if (dto == null) return null;
        return new LogInteractionCommand(
            journalistId,
            dto.getActivityId(),
            dto.getDate(),
            dto.getDescription(),
            null
        );
    }

    private Set<ThemeDTO> toThemeDTOSet(List<Theme> themes) {
        if (themes == null) return null;
        Set<ThemeDTO> set = new LinkedHashSet<>(themes.size());
        for (Theme t : themes) set.add(toDto(t));
        return set;
    }
}
