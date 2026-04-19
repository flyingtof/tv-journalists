package org.terrevivante.tvjournalists.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JournalistCreateDTO {
    private String firstName;
    private String lastName;
    private String globalEmail;
    private String globalPhone;
}
