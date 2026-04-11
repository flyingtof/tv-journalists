package org.terrevivante.tvjournalists.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
public class InteractionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journalist_id", nullable = false)
    private Journalist journalist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private Activity activity;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String description;

    private UUID createdBy;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    public InteractionLog() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Journalist getJournalist() { return journalist; }
    public void setJournalist(Journalist journalist) { this.journalist = journalist; }
    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
