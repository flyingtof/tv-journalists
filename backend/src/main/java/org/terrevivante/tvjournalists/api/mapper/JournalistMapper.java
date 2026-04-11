package org.terrevivante.tvjournalists.api.mapper;

import org.terrevivante.tvjournalists.api.dto.*;
import org.terrevivante.tvjournalists.application.command.CreateJournalistCommand;
import org.terrevivante.tvjournalists.application.command.LogInteractionCommand;
import org.terrevivante.tvjournalists.domain.model.*;

import java.util.UUID;

public interface JournalistMapper {
    JournalistDTO toDto(Journalist journalist);
    ActivityDTO toDto(Activity activity);
    ThemeDTO toDto(Theme theme);
    InteractionDTO toDto(InteractionLog log);
    CreateJournalistCommand toCommand(JournalistCreateDTO dto);
    LogInteractionCommand toCommand(UUID journalistId, InteractionCreateDTO dto);
}
