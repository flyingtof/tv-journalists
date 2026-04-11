package org.terrevivante.tvjournalists.domain;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journalist_id", nullable = false)
    private Journalist journalist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    private String role;
    private String specificEmail;
    private String specificPhone;

    @ManyToMany
    @JoinTable(
        name = "activity_themes",
        joinColumns = @JoinColumn(name = "activity_id"),
        inverseJoinColumns = @JoinColumn(name = "theme_id")
    )
    private Set<Theme> themes = new HashSet<>();

    public Activity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Journalist getJournalist() { return journalist; }
    public void setJournalist(Journalist journalist) { this.journalist = journalist; }
    public Media getMedia() { return media; }
    public void setMedia(Media media) { this.media = media; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecificEmail() { return specificEmail; }
    public void setSpecificEmail(String specificEmail) { this.specificEmail = specificEmail; }
    public String getSpecificPhone() { return specificPhone; }
    public void setSpecificPhone(String specificPhone) { this.specificPhone = specificPhone; }
    public Set<Theme> getThemes() { return themes; }
    public void setThemes(Set<Theme> themes) { this.themes = themes; }
}
