package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataJournalistRepository
        extends JpaRepository<JournalistEntity, UUID>, JpaSpecificationExecutor<JournalistEntity> {

    @Override
    Page<JournalistEntity> findAll(Specification<JournalistEntity> spec, Pageable pageable);

    @Query("select distinct j from JournalistEntity j " +
           "left join fetch j.activities a " +
           "left join fetch a.media " +
           "where j.id in :ids")
    List<JournalistEntity> findWithActivitiesByIds(@Param("ids") List<UUID> ids);

    @Query("select distinct j from JournalistEntity j " +
           "left join fetch j.activities a " +
           "left join fetch a.media " +
           "where j.id = :id")
    Optional<JournalistEntity> findWithActivitiesById(@Param("id") UUID id);
}
