package org.terrevivante.tvjournalists.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.terrevivante.tvjournalists.domain.model.MediaType;

import java.util.UUID;

@Entity
@Table(name = "media")
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MediaType type;

    @Column(name = "url")
    private String url;

    public MediaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public MediaType getType() { return type; }
    public void setType(MediaType type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
