package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JournalistCreateDTO {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    private String globalEmail;
    private String globalPhone;
}
