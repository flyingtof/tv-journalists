package org.terrevivante.tvjournalists.domain;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public Theme() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
