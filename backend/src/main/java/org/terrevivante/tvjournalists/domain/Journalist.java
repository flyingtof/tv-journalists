package org.terrevivante.tvjournalists.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class Journalist {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String globalEmail;
    private String globalPhone;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "journalist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "journalist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InteractionLog> interactions = new ArrayList<>();

    public Journalist() {}

    public Journalist(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getGlobalEmail() { return globalEmail; }
    public void setGlobalEmail(String globalEmail) { this.globalEmail = globalEmail; }
    public String getGlobalPhone() { return globalPhone; }
    public void setGlobalPhone(String globalPhone) { this.globalPhone = globalPhone; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Activity> getActivities() { return activities; }
    public void setActivities(List<Activity> activities) { this.activities = activities; }
    public List<InteractionLog> getInteractions() { return interactions; }
    public void setInteractions(List<InteractionLog> interactions) { this.interactions = interactions; }
}
