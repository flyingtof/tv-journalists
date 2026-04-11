package org.terrevivante.tvjournalists.infrastructure.persistence.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activity")
public class ActivityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journalist_id", nullable = false)
    private JournalistEntity journalist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private MediaEntity media;

    @Column(name = "role")
    private String role;

    @Column(name = "specific_email")
    private String specificEmail;

    @Column(name = "specific_phone")
    private String specificPhone;

    @ManyToMany
    @JoinTable(
        name = "activity_themes",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    private List<ThemeEntity> themes = new ArrayList<>();

    public ActivityEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public JournalistEntity getJournalist() { return journalist; }
    public void setJournalist(JournalistEntity journalist) { this.journalist = journalist; }
    public MediaEntity getMedia() { return media; }
    public void setMedia(MediaEntity media) { this.media = media; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecificEmail() { return specificEmail; }
    public void setSpecificEmail(String specificEmail) { this.specificEmail = specificEmail; }
    public String getSpecificPhone() { return specificPhone; }
    public void setSpecificPhone(String specificPhone) { this.specificPhone = specificPhone; }
    public List<ThemeEntity> getThemes() { return themes; }
    public void setThemes(List<ThemeEntity> themes) { this.themes = themes; }
}
