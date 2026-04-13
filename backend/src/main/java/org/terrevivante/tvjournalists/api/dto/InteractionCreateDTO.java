package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class InteractionCreateDTO {
    @NotNull
    private LocalDate date;
    @NotBlank
    private String description;
    private UUID activityId;
}
