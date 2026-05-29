package org.terrevivante.tvjournalists.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class JournalistActivityUpsertDTO {
    private UUID id;
    private UUID mediaId;
    private String specificEmail;
    private String specificPhone;
    private List<UUID> themeIds = new ArrayList<>();
}
