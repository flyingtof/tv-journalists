package org.terrevivante.tvjournalists.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InteractionCreateDTO {
    private LocalDate date;
    private String description;
    private UUID activityId;
}
