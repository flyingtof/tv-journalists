package org.terrevivante.tvjournalists.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public class InteractionDTO {
    private UUID id;
    private LocalDate date;
    private String description;
    private UUID activityId;

    public InteractionDTO() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getActivityId() { return activityId; }
    public void setActivityId(UUID activityId) { this.activityId = activityId; }
}
