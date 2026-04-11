package org.terrevivante.tvjournalists.persistence;

import org.terrevivante.tvjournalists.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    // Load activities with their themes in a single query for a batch of activity ids
    @Query("select distinct a from Activity a left join fetch a.themes where a.id in :ids")
    List<Activity> findWithThemesByIds(@Param("ids") List<UUID> ids);
}

