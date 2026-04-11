package org.terrevivante.tvjournalists.api.dto;

import java.util.Set;
import java.util.UUID;

public class ActivityDTO {
    private UUID id;
    private UUID mediaId;
    private String mediaName;
    private String role;
    private String specificEmail;
    private String specificPhone;
    private Set<ThemeDTO> themes;

    public ActivityDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMediaId() { return mediaId; }
    public void setMediaId(UUID mediaId) { this.mediaId = mediaId; }
    public String getMediaName() { return mediaName; }
    public void setMediaName(String mediaName) { this.mediaName = mediaName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getSpecificEmail() { return specificEmail; }
    public void setSpecificEmail(String specificEmail) { this.specificEmail = specificEmail; }
    public String getSpecificPhone() { return specificPhone; }
    public void setSpecificPhone(String specificPhone) { this.specificPhone = specificPhone; }
    public Set<ThemeDTO> getThemes() { return themes; }
    public void setThemes(Set<ThemeDTO> themes) { this.themes = themes; }
}
