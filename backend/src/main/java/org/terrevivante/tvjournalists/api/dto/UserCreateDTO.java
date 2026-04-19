package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserCreateDTO {
    @NotBlank
    private String username;
    @NotBlank
    @Size(min = 8)
    private String password;
    private String firstName;
    private String lastName;
    @NotNull
    private Boolean enabled;
    @NotEmpty
    private Set<Role> roles;
}
