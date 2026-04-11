package org.terrevivante.tvjournalists.api.dto;

import java.util.List;
import java.util.UUID;

public class JournalistDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String globalEmail;
    private String globalPhone;
    private List<ActivityDTO> activities;

    public JournalistDTO() {}

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
    public List<ActivityDTO> getActivities() { return activities; }
    public void setActivities(List<ActivityDTO> activities) { this.activities = activities; }
}
