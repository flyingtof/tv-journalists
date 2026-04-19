package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetDTO {
    @NotBlank
    @Size(min = 8)
    private String password;
}
