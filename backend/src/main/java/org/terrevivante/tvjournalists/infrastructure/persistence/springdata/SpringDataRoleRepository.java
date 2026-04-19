package org.terrevivante.tvjournalists.infrastructure.persistence.springdata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.RoleEntity;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface SpringDataRoleRepository extends JpaRepository<RoleEntity, UUID> {
    Set<RoleEntity> findByCodeIn(Collection<String> codes);
}
