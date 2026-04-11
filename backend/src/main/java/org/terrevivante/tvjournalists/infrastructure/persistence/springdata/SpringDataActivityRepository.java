package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;

import java.util.List;
import java.util.UUID;

public interface SpringDataActivityRepository extends JpaRepository<ActivityEntity, UUID> {

    @Query("select distinct a from ActivityEntity a left join fetch a.themes where a.id in :ids")
    List<ActivityEntity> findWithThemesByIds(@Param("ids") List<UUID> ids);
}
