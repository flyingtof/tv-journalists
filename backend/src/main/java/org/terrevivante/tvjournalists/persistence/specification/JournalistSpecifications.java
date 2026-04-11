package org.terrevivante.tvjournalists.persistence.specification;

import org.terrevivante.tvjournalists.domain.Journalist;
import org.terrevivante.tvjournalists.domain.Activity;
import org.terrevivante.tvjournalists.domain.Media;
import org.terrevivante.tvjournalists.domain.Theme;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class JournalistSpecifications {

    public static Specification<Journalist> hasName(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) return null;
            String pattern = "%" + name.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern)
            );
        };
    }

    public static Specification<Journalist> hasMedia(List<String> mediaNames) {
        return (root, query, cb) -> {
            if (mediaNames == null || mediaNames.isEmpty()) return null;
            Join<Journalist, Activity> activities = root.join("activities", JoinType.LEFT);
            Join<Activity, Media> media = activities.join("media", JoinType.LEFT);
            return media.get("name").in(mediaNames);
        };
    }

    public static Specification<Journalist> hasThemes(List<String> themeNames) {
        return (root, query, cb) -> {
            if (themeNames == null || themeNames.isEmpty()) return null;
            Join<Journalist, Activity> activities = root.join("activities", JoinType.LEFT);
            Join<Activity, Theme> themes = activities.join("themes", JoinType.LEFT);
            return themes.get("name").in(themeNames);
        };
    }
}
