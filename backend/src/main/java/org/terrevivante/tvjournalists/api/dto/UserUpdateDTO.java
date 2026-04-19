package org.terrevivante.tvjournalists.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateDTO {
    private String firstName;
    private String lastName;
    @NotNull
    private Boolean enabled;
    @NotEmpty
    private Set<Role> roles;
}
