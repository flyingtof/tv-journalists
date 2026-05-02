package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataThemeRepository extends JpaRepository<ThemeEntity, UUID> {
    Optional<ThemeEntity> findByNameIgnoreCase(String name);

    @Query("""
        select count(t) > 0
        from ActivityEntity a
        join a.themes t
        where t.id = :id
        """)
    boolean existsInActivityById(@Param("id") UUID id);
}
