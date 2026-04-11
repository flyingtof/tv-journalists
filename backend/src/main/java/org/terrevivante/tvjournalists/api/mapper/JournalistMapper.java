package org.terrevivante.tvjournalists.api.mapper;

import org.terrevivante.tvjournalists.api.dto.*;
import org.terrevivante.tvjournalists.domain.Activity;
import org.terrevivante.tvjournalists.domain.InteractionLog;
import org.terrevivante.tvjournalists.domain.Journalist;
import org.terrevivante.tvjournalists.domain.Theme;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface JournalistMapper {

    JournalistDTO toDto(Journalist journalist);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "activities", ignore = true)
    @Mapping(target = "interactions", ignore = true)
    Journalist toEntity(JournalistCreateDTO dto);

    @Mapping(target = "mediaId", source = "media.id")
    @Mapping(target = "mediaName", source = "media.name")
    ActivityDTO toDto(Activity activity);

    ThemeDTO toDto(Theme theme);

    @Mapping(target = "activityId", source = "activity.id")
    InteractionDTO toDto(InteractionLog log);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "journalist", ignore = true)
    @Mapping(target = "activity", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    InteractionLog toEntity(InteractionCreateDTO dto);
}
