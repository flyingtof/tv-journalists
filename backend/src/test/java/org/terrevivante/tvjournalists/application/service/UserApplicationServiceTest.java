package org.terrevivante.tvjournalists.application.service;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.terrevivante.tvjournalists.application.command.CreateUserCommand;
import org.terrevivante.tvjournalists.application.command.ResetUserPasswordCommand;
import org.terrevivante.tvjournalists.application.command.UpdateUserCommand;
import org.terrevivante.tvjournalists.application.exception.UserAlreadyExistsException;
import org.terrevivante.tvjournalists.application.exception.UserNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.model.Role;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserApplicationServiceTest {

    private final ApplicationUserRepository userRepository = mock(ApplicationUserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ApplicationValidator applicationValidator =
        new ApplicationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    private final UserApplicationService service =
        new UserApplicationService(userRepository, passwordEncoder, applicationValidator);

    @Test
    void shouldCreateUserWithHashedPassword() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-secret");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser result = service.create(new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            true,
            Set.of(Role.ADMIN)
        ));

        assertThat(result.passwordHash()).isEqualTo("hashed-secret");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(new ApplicationUser(
            null,
            "alice",
            "hashed-secret",
            "Alice",
            "Green",
            true,
            Set.of(Role.ADMIN)
        ));
    }

    @Test
    void shouldRejectDuplicateUsernameWhenCreatingUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser()));

        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            true,
            Set.of(Role.USER)
        )))
            .isInstanceOf(UserAlreadyExistsException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldRejectCreateUserWithShortPasswordBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            "alice",
            "short",
            "Alice",
            "Green",
            true,
            Set.of(Role.USER)
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectCreateUserWithoutRolesBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            true,
            null
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("roles"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectCreateUserWithEmptyRolesBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            true,
            Set.of()
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("roles"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectCreateUserWithBlankUsernameBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            " ",
            "secret123",
            "Alice",
            "Green",
            true,
            Set.of(Role.USER)
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("username"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectCreateUserWhenEnabledIsMissingBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.create(new CreateUserCommand(
            "alice",
            "secret123",
            "Alice",
            "Green",
            null,
            Set.of(Role.USER)
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("enabled"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldListAllUsers() {
        ApplicationUser user = existingUser();
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<ApplicationUser> result = service.list();

        assertThat(result).containsExactly(user);
    }

    @Test
    void shouldUpdateUserWithoutChangingPasswordHashOrUsername() {
        ApplicationUser existingUser = existingUser();
        when(userRepository.findById(existingUser.id())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser result = service.update(new UpdateUserCommand(
            existingUser.id(),
            "Alicia",
            "Brown",
            false,
            Set.of(Role.ADMIN, Role.USER)
        ));

        assertThat(result).isEqualTo(new ApplicationUser(
            existingUser.id(),
            "alice",
            "stored-hash",
            "Alicia",
            "Brown",
            false,
            Set.of(Role.ADMIN, Role.USER)
        ));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateUserCommand(
            userId,
            "Alicia",
            "Brown",
            true,
            Set.of(Role.USER)
        )))
            .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldRejectUpdateUserWithoutRolesBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.update(new UpdateUserCommand(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "Alicia",
            "Brown",
            true,
            null
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("roles"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectUpdateUserWhenEnabledIsMissingBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.update(new UpdateUserCommand(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "Alicia",
            "Brown",
            null,
            Set.of(Role.USER)
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("enabled"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldRejectUpdateUserWithEmptyRolesBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.update(new UpdateUserCommand(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "Alicia",
            "Brown",
            true,
            Set.of()
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("roles"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldResetPasswordWithHashedValue() {
        ApplicationUser existingUser = existingUser();
        when(userRepository.findById(existingUser.id())).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationUser result = service.resetPassword(new ResetUserPasswordCommand(existingUser.id(), "new-secret"));

        assertThat(result.passwordHash()).isEqualTo("new-hash");
        assertThat(result.username()).isEqualTo(existingUser.username());
        verify(passwordEncoder).encode("new-secret");
    }

    @Test
    void shouldThrowWhenResettingPasswordForUnknownUser() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(new ResetUserPasswordCommand(userId, "new-secret")))
            .isInstanceOf(UserNotFoundException.class);

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }


    @Test
    void shouldRejectPasswordResetWithShortPasswordBeforeRepositoryInteraction() {
        assertThatThrownBy(() -> service.resetPassword(new ResetUserPasswordCommand(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "short"
        )))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class)
            .satisfies(exception -> assertThat(((jakarta.validation.ConstraintViolationException) exception).getConstraintViolations())
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("password"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void shouldGetCurrentUserByUsername() {
        ApplicationUser existingUser = existingUser();
        when(userRepository.findByUsername(existingUser.username())).thenReturn(Optional.of(existingUser));

        ApplicationUser result = service.getCurrentUser(existingUser.username());

        assertThat(result).isEqualTo(existingUser);
    }

    @Test
    void shouldThrowWhenCurrentUserCannotBeFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getCurrentUser("missing"))
            .isInstanceOf(UserNotFoundException.class);
    }

    private ApplicationUser existingUser() {
        return new ApplicationUser(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "alice",
            "stored-hash",
            "Alice",
            "Green",
            true,
            Set.of(Role.USER)
        );
    }
}
