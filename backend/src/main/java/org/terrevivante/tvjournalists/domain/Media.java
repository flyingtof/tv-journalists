package org.terrevivante.tvjournalists.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Media {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType type;

    private String url;

    public enum MediaType {
        RADIO, TV, PRESS, WEB, SOCIAL_NETWORK
    }

    public Media() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
