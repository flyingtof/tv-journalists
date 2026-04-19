package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.model.Role;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ApplicationUserEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.RoleEntity;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ApplicationUserRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldGenerateIdsForNewPersistedEntities() {
        RoleEntity role = new RoleEntity();
        role.setCode("TEMP_ROLE");
        role.setLabel("Temporary role");
        entityManager.persist(role);

        ApplicationUserEntity user = new ApplicationUserEntity();
        user.setUsername("generated-id-user");
        user.setPasswordHash("$2a$10$abcdefghijklmnopqrstuv");
        user.setFirstName("Generated");
        user.setLastName("User");
        user.setEnabled(true);
        user.setRoles(Set.of(role));
        entityManager.persist(user);

        entityManager.flush();

        assertThat(role.getId()).isNotNull();
        assertThat(user.getId()).isNotNull();
    }

    @Test
    void shouldSaveAndReloadUserWithRoles() {
        ApplicationUser created = applicationUserRepository.save(new ApplicationUser(
            null,
            "admin-role-test",
            "$2a$10$abcdefghijklmnopqrstuv",
            "Alice",
            "Admin",
            true,
            Set.of(Role.ADMIN, Role.USER)
        ));

        entityManager.flush();
        entityManager.clear();

        ApplicationUser reloaded = applicationUserRepository.findByUsername("admin-role-test").orElseThrow();

        assertThat(reloaded.id()).isEqualTo(created.id());
        assertThat(reloaded.roles()).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
        assertThat(reloaded.enabled()).isTrue();
    }

    @Test
    void shouldRejectUnknownExplicitIdWhenSavingNewUser() {
        UUID requestedId = UUID.randomUUID();

        assertThatThrownBy(() -> applicationUserRepository.save(new ApplicationUser(
            requestedId,
            "explicit-id-user",
            "$2a$10$abcdefghijklmnopqrstuv",
            "Eve",
            "Explicit",
            true,
            Set.of(Role.USER)
        )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(requestedId.toString());
    }

    @Test
    void shouldKeepEmptyRoleAssociationMutableForManagedEntity() {
        ApplicationUser created = applicationUserRepository.save(new ApplicationUser(
            null,
            "no-roles",
            "$2a$10$abcdefghijklmnopqrstuv",
            "Nora",
            "Roles",
            true,
            Set.of()
        ));

        ApplicationUserEntity managedUser = entityManager.find(ApplicationUserEntity.class, created.id());
        RoleEntity adminRole = entityManager.createQuery(
                "select role from RoleEntity role where role.code = :code",
                RoleEntity.class
            )
            .setParameter("code", Role.ADMIN.name())
            .getSingleResult();

        assertThatCode(() -> managedUser.getRoles().add(adminRole))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldFailWhenRequestedRoleDoesNotExist() {
        entityManager.createNativeQuery("DELETE FROM user_role").executeUpdate();
        entityManager.createNativeQuery("DELETE FROM role WHERE code = 'ADMIN'").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> applicationUserRepository.save(new ApplicationUser(
            null,
            "missing-role",
            "$2a$10$abcdefghijklmnopqrstuv",
            "Bob",
            "Builder",
            true,
            Set.of(Role.ADMIN)
        )))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("ADMIN");
    }

    @Test
    void migrationShouldSeedBootstrapAdmin() {
        ApplicationUser admin = applicationUserRepository.findByUsername("admin").orElseThrow();

        assertThat(admin.roles()).contains(Role.ADMIN, Role.USER);
        assertThat(admin.enabled()).isTrue();
    }
}
