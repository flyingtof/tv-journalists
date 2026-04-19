package org.terrevivante.tvjournalists.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.terrevivante.tvjournalists.api.dto.CurrentUserDTO;
import org.terrevivante.tvjournalists.api.dto.PasswordResetDTO;
import org.terrevivante.tvjournalists.api.dto.UserCreateDTO;
import org.terrevivante.tvjournalists.api.dto.UserDTO;
import org.terrevivante.tvjournalists.api.dto.UserUpdateDTO;
import org.terrevivante.tvjournalists.application.command.CreateUserCommand;
import org.terrevivante.tvjournalists.application.command.ResetUserPasswordCommand;
import org.terrevivante.tvjournalists.application.command.UpdateUserCommand;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.model.Role;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToSortedList")
    UserDTO toDto(ApplicationUser user);

    @Mapping(source = "roles", target = "roles", qualifiedByName = "rolesToSortedList")
    CurrentUserDTO toCurrentUserDto(ApplicationUser user);

    CreateUserCommand toCommand(UserCreateDTO dto);

    default UpdateUserCommand toCommand(UUID id, UserUpdateDTO dto) {
        if (dto == null) return null;
        return mapToUpdateUserCommand(id, dto);
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "dto.firstName", target = "firstName")
    @Mapping(source = "dto.lastName", target = "lastName")
    @Mapping(source = "dto.enabled", target = "enabled")
    @Mapping(source = "dto.roles", target = "roles")
    UpdateUserCommand mapToUpdateUserCommand(UUID id, UserUpdateDTO dto);

    default ResetUserPasswordCommand toCommand(UUID id, PasswordResetDTO dto) {
        if (dto == null) return null;
        return mapToResetUserPasswordCommand(id, dto);
    }

    @Mapping(source = "id", target = "id")
    @Mapping(source = "dto.password", target = "password")
    ResetUserPasswordCommand mapToResetUserPasswordCommand(UUID id, PasswordResetDTO dto);

    @Named("rolesToSortedList")
    default List<String> rolesToSortedList(Set<Role> roles) {
        if (roles == null) return List.of();
        return roles.stream()
            .map(Role::name)
            .sorted()
            .toList();
    }
}
