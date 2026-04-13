package org.terrevivante.tvjournalists.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activity")
@Getter
@Setter
@NoArgsConstructor
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
}
