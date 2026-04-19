package org.terrevivante.tvjournalists.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.terrevivante.tvjournalists.api.dto.PasswordResetDTO;
import org.terrevivante.tvjournalists.api.dto.UserCreateDTO;
import org.terrevivante.tvjournalists.api.dto.UserDTO;
import org.terrevivante.tvjournalists.api.dto.UserUpdateDTO;
import org.terrevivante.tvjournalists.api.mapper.UserMapper;
import org.terrevivante.tvjournalists.application.usecase.CreateUserUseCase;
import org.terrevivante.tvjournalists.application.usecase.ListUsersUseCase;
import org.terrevivante.tvjournalists.application.usecase.ResetUserPasswordUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateUserUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final ListUsersUseCase listUsersUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final ResetUserPasswordUseCase resetUserPasswordUseCase;
    private final UserMapper userMapper;

    public UserController(ListUsersUseCase listUsersUseCase,
                          CreateUserUseCase createUserUseCase,
                          UpdateUserUseCase updateUserUseCase,
                          ResetUserPasswordUseCase resetUserPasswordUseCase,
                          UserMapper userMapper) {
        this.listUsersUseCase = listUsersUseCase;
        this.createUserUseCase = createUserUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.resetUserPasswordUseCase = resetUserPasswordUseCase;
        this.userMapper = userMapper;
    }

    @GetMapping
    public List<UserDTO> listUsers() {
        return listUsersUseCase.list().stream()
            .map(userMapper::toDto)
            .toList();
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userMapper.toDto(createUserUseCase.create(userMapper.toCommand(userCreateDTO))));
    }

    @PutMapping("/{id}")
    public UserDTO updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return userMapper.toDto(updateUserUseCase.update(userMapper.toCommand(id, userUpdateDTO)));
    }

    @PostMapping("/{id}/password-reset")
    public UserDTO resetPassword(@PathVariable UUID id, @Valid @RequestBody PasswordResetDTO passwordResetDTO) {
        return userMapper.toDto(resetUserPasswordUseCase.resetPassword(userMapper.toCommand(id, passwordResetDTO)));
    }
}
