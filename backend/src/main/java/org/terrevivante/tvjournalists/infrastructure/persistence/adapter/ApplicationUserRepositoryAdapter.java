package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.model.Role;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ApplicationUserEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.RoleEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataApplicationUserRepository;
import org.terrevivante.tvjournalists.infrastructure.persistence.springdata.SpringDataRoleRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class ApplicationUserRepositoryAdapter implements ApplicationUserRepository {

    private final SpringDataApplicationUserRepository userRepository;
    private final SpringDataRoleRepository roleRepository;

    public ApplicationUserRepositoryAdapter(SpringDataApplicationUserRepository userRepository,
                                            SpringDataRoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ApplicationUser save(ApplicationUser user) {
        Optional<ApplicationUserEntity> existingEntity = user.id() == null
            ? Optional.empty()
            : userRepository.findById(user.id());

        if (user.id() != null && existingEntity.isEmpty()) {
            throw new IllegalArgumentException("User not found: " + user.id());
        }

        ApplicationUserEntity entity = existingEntity.orElseGet(ApplicationUserEntity::new);

        entity.setUsername(user.username());
        entity.setPasswordHash(user.passwordHash());
        entity.setFirstName(user.firstName());
        entity.setLastName(user.lastName());
        entity.setEnabled(user.enabled());
        entity.setRoles(resolveRoles(user.roles()));

        return toDomain(userRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationUser> findById(UUID id) {
        return userRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ApplicationUser> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationUser> findAll() {
        return userRepository.findAll().stream()
            .map(this::toDomain)
            .toList();
    }

    private Set<RoleEntity> resolveRoles(Set<Role> roles) {
        if (roles.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> requestedCodes = roles.stream()
            .map(Role::name)
            .collect(Collectors.toSet());
        Set<RoleEntity> resolvedRoles = roleRepository.findByCodeIn(requestedCodes);
        Set<String> resolvedCodes = resolvedRoles.stream()
            .map(RoleEntity::getCode)
            .collect(Collectors.toSet());

        if (!resolvedCodes.containsAll(requestedCodes)) {
            Set<String> missingCodes = new HashSet<>(requestedCodes);
            missingCodes.removeAll(resolvedCodes);
            throw new IllegalArgumentException("Roles not found: " + String.join(", ", missingCodes));
        }

        return resolvedRoles;
    }

    private ApplicationUser toDomain(ApplicationUserEntity entity) {
        return new ApplicationUser(
            entity.getId(),
            entity.getUsername(),
            entity.getPasswordHash(),
            entity.getFirstName(),
            entity.getLastName(),
            entity.isEnabled(),
            entity.getRoles().stream()
                .map(RoleEntity::getCode)
                .map(Role::valueOf)
                .collect(Collectors.toSet())
        );
    }
}
