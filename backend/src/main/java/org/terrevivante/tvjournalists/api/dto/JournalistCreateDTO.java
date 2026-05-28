package org.terrevivante.tvjournalists.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JournalistCreateDTO {
    private String firstName;
    private String lastName;
    private String globalEmail;
    private String globalPhone;
    private List<JournalistActivityUpsertDTO> activities = new ArrayList<>();
}
