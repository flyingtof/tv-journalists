package org.terrevivante.tvjournalists.api.dto;

import org.terrevivante.tvjournalists.domain.model.MediaType;

import java.util.UUID;

public class MediaDTO {
    private UUID id;
    private String name;
    private MediaType type;
    private String url;

    public MediaDTO() {}

    public MediaDTO(UUID id, String name, MediaType type, String url) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.url = url;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
