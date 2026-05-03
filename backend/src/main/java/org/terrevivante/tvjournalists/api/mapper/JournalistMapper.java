package org.terrevivante.tvjournalists.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.terrevivante.tvjournalists.api.dto.ActivityDTO;
import org.terrevivante.tvjournalists.api.dto.InteractionCreateDTO;
import org.terrevivante.tvjournalists.api.dto.InteractionDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistCreateDTO;
import org.terrevivante.tvjournalists.api.dto.JournalistDTO;
import org.terrevivante.tvjournalists.api.dto.ThemeDTO;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.application.readmodel.ActivityView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.application.readmodel.ThemeView;
import org.terrevivante.tvjournalists.domain.model.Activity;
import org.terrevivante.tvjournalists.domain.model.InteractionLog;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.model.Theme;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface JournalistMapper {

    JournalistDTO toDto(Journalist journalist);

    JournalistDTO toDto(JournalistListItemView journalist);

    JournalistDTO toDto(JournalistProfileView journalist);

    @Mapping(source = "media.id", target = "mediaId")
    @Mapping(source = "media.name", target = "mediaName")
    @Mapping(source = "themes", target = "themes", qualifiedByName = "themesToOrderedSet")
    ActivityDTO toDto(Activity activity);

    @Mapping(source = "media.id", target = "mediaId")
    @Mapping(source = "media.name", target = "mediaName")
    @Mapping(source = "themes", target = "themes", qualifiedByName = "themeViewsToOrderedSet")
    ActivityDTO toDto(ActivityView activity);

    ThemeDTO toDto(Theme theme);

    ThemeDTO toDto(ThemeView theme);

    InteractionDTO toDto(InteractionLog log);

    CreateJournalistCommand toCommand(JournalistCreateDTO dto);

    /** Null-guards {@code dto} before delegating to the generated multi-source method. */
    default LogInteractionCommand toCommand(UUID journalistId, InteractionCreateDTO dto) {
        if (dto == null) return null;
        return mapToLogInteractionCommand(journalistId, dto);
    }

    @Mapping(source = "journalistId", target = "journalistId")
    @Mapping(source = "dto.activityId", target = "activityId")
    @Mapping(source = "dto.date", target = "date")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(target = "createdBy", ignore = true)
    LogInteractionCommand mapToLogInteractionCommand(UUID journalistId, InteractionCreateDTO dto);

    @Named("themesToOrderedSet")
    default Set<ThemeDTO> themesToOrderedSet(List<Theme> themes) {
        if (themes == null) return Collections.emptySet();
        return themes.stream()
            .map(this::toDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Named("themeViewsToOrderedSet")
    default Set<ThemeDTO> themeViewsToOrderedSet(List<ThemeView> themes) {
        if (themes == null) return Collections.emptySet();
        return themes.stream()
            .map(this::toDto)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
