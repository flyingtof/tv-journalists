package org.terrevivante.tvjournalists.persistence;

import org.terrevivante.tvjournalists.domain.Journalist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JournalistRepository extends JpaRepository<Journalist, UUID>, JpaSpecificationExecutor<Journalist> {

    // Note: do NOT fetch collection associations when using pagination (Hibernate warns and applies in-memory pagination).
    // Keep the default paginated query without collection fetch to allow DB-side pagination.
    @Override
    Page<Journalist> findAll(Specification<Journalist> spec, Pageable pageable);

    // When we need to initialize collections for a given page of journalists, use a dedicated query
    // that fetches collections for a specific set of ids (no pagination here).
    @Query("select distinct j from Journalist j " +
           "left join fetch j.activities a " +
           "left join fetch a.media " +
           "where j.id in :ids")
    List<Journalist> findWithActivitiesByIds(@Param("ids") List<UUID> ids);

    // Fetch a single journalist with its activities, media and themes to avoid N+1 selects when displaying details
    @Query("select distinct j from Journalist j " +
           "left join fetch j.activities a " +
           "left join fetch a.media " +
           "where j.id = :id")
    Optional<Journalist> findWithActivitiesById(@Param("id") UUID id);
}
