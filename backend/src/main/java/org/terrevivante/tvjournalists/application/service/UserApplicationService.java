package org.terrevivante.tvjournalists.application.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.terrevivante.tvjournalists.application.command.CreateUserCommand;
import org.terrevivante.tvjournalists.application.command.ResetUserPasswordCommand;
import org.terrevivante.tvjournalists.application.command.UpdateUserCommand;
import org.terrevivante.tvjournalists.application.exception.UserAlreadyExistsException;
import org.terrevivante.tvjournalists.application.exception.UserNotFoundException;
import org.terrevivante.tvjournalists.application.usecase.CreateUserUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetCurrentUserUseCase;
import org.terrevivante.tvjournalists.application.usecase.ListUsersUseCase;
import org.terrevivante.tvjournalists.application.usecase.ResetUserPasswordUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateUserUseCase;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;

import java.util.List;
import java.util.UUID;

public class UserApplicationService
    implements CreateUserUseCase, ListUsersUseCase, UpdateUserUseCase, ResetUserPasswordUseCase, GetCurrentUserUseCase {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationValidator applicationValidator;

    public UserApplicationService(ApplicationUserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  ApplicationValidator applicationValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationValidator = applicationValidator;
    }

    @Override
    public ApplicationUser create(CreateUserCommand command) {
        applicationValidator.validate(command);

        if (userRepository.findByUsername(command.username()).isPresent()) {
            throw new UserAlreadyExistsException(command.username());
        }

        ApplicationUser user = new ApplicationUser(
            null,
            command.username(),
            passwordEncoder.encode(command.password()),
            command.firstName(),
            command.lastName(),
            command.enabled(),
            command.roles()
        );
        return userRepository.save(user);
    }

    @Override
    public List<ApplicationUser> list() {
        return userRepository.findAll();
    }

    @Override
    public ApplicationUser update(UpdateUserCommand command) {
        applicationValidator.validate(command);
        ApplicationUser existingUser = getById(command.id());

        ApplicationUser user = new ApplicationUser(
            existingUser.id(),
            existingUser.username(),
            existingUser.passwordHash(),
            command.firstName(),
            command.lastName(),
            command.enabled(),
            command.roles()
        );
        return userRepository.save(user);
    }

    @Override
    public ApplicationUser resetPassword(ResetUserPasswordCommand command) {
        applicationValidator.validate(command);
        ApplicationUser existingUser = getById(command.id());

        ApplicationUser user = new ApplicationUser(
            existingUser.id(),
            existingUser.username(),
            passwordEncoder.encode(command.password()),
            existingUser.firstName(),
            existingUser.lastName(),
            existingUser.enabled(),
            existingUser.roles()
        );
        return userRepository.save(user);
    }

    @Override
    public ApplicationUser getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
    }

    private ApplicationUser getById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }
}
