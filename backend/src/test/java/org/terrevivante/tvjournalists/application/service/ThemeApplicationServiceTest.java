package org.terrevivante.tvjournalists.application.service;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.application.command.CreateThemeCommand;
import org.terrevivante.tvjournalists.application.command.UpdateThemeCommand;
import org.terrevivante.tvjournalists.application.exception.ThemeAlreadyExistsException;
import org.terrevivante.tvjournalists.application.exception.ThemeInUseException;
import org.terrevivante.tvjournalists.application.exception.ThemeNotFoundException;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ThemeApplicationServiceTest {

    private final ThemeRepository themeRepository = mock(ThemeRepository.class);
    private final ApplicationValidator applicationValidator =
        new ApplicationValidator(Validation.buildDefaultValidatorFactory().getValidator());
    private final ThemeApplicationService service =
        new ThemeApplicationService(themeRepository, applicationValidator);

    @Test
    void shouldCreateThemeWithTrimmedName() {
        when(themeRepository.findByNameIgnoreCase("Politics")).thenReturn(Optional.empty());
        when(themeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Theme result = service.create(new CreateThemeCommand("  Politics  "));

        assertThat(result).isEqualTo(new Theme(null, "Politics"));
        verify(themeRepository).findByNameIgnoreCase("Politics");
        verify(themeRepository).save(new Theme(null, "Politics"));
    }

    @Test
    void shouldRejectDuplicateThemeWhenCreating() {
        Theme existingTheme = new Theme(UUID.randomUUID(), "Politics");
        when(themeRepository.findByNameIgnoreCase("Politics")).thenReturn(Optional.of(existingTheme));

        assertThatThrownBy(() -> service.create(new CreateThemeCommand(" Politics ")))
            .isInstanceOf(ThemeAlreadyExistsException.class);

        verify(themeRepository, never()).save(any());
    }

    @Test
    void shouldRejectDuplicateThemeWhenCreatingIgnoringCase() {
        Theme existingTheme = new Theme(UUID.randomUUID(), "Politics");
        when(themeRepository.findByNameIgnoreCase("POLITICS")).thenReturn(Optional.of(existingTheme));

        assertThatThrownBy(() -> service.create(new CreateThemeCommand("  POLITICS  ")))
            .isInstanceOf(ThemeAlreadyExistsException.class);

        verify(themeRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidThemeWhenCreating() {
        assertThatThrownBy(() -> service.create(new CreateThemeCommand("  ")))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class);

        verifyNoInteractions(themeRepository);
    }

    @Test
    void shouldUpdateThemeWithTrimmedName() {
        UUID themeId = UUID.randomUUID();
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(new Theme(themeId, "Politics")));
        when(themeRepository.findByNameIgnoreCase("Economy")).thenReturn(Optional.empty());
        when(themeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Theme result = service.update(new UpdateThemeCommand(themeId, "  Economy  "));

        assertThat(result).isEqualTo(new Theme(themeId, "Economy"));
        verify(themeRepository).findByNameIgnoreCase("Economy");
        verify(themeRepository).save(new Theme(themeId, "Economy"));
    }

    @Test
    void shouldRejectUpdateWhenThemeIsMissing() {
        UUID themeId = UUID.randomUUID();
        when(themeRepository.findById(themeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(new UpdateThemeCommand(themeId, "Economy")))
            .isInstanceOf(ThemeNotFoundException.class);

        verify(themeRepository, never()).findByNameIgnoreCase(any());
        verify(themeRepository, never()).save(any());
    }

    @Test
    void shouldRejectInvalidThemeWhenUpdatingWithNullId() {
        assertThatThrownBy(() -> service.update(new UpdateThemeCommand(null, "Economy")))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class);

        verifyNoInteractions(themeRepository);
    }

    @Test
    void shouldRejectInvalidThemeWhenUpdatingWithBlankName() {
        UUID themeId = UUID.randomUUID();

        assertThatThrownBy(() -> service.update(new UpdateThemeCommand(themeId, " ")))
            .isInstanceOf(jakarta.validation.ConstraintViolationException.class);

        verifyNoInteractions(themeRepository);
    }

    @Test
    void shouldRejectUpdateWhenNameConflictsWithAnotherTheme() {
        UUID themeId = UUID.randomUUID();
        Theme existingTheme = new Theme(themeId, "Politics");
        Theme conflictingTheme = new Theme(UUID.randomUUID(), "Economy");
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(existingTheme));
        when(themeRepository.findByNameIgnoreCase("Economy")).thenReturn(Optional.of(conflictingTheme));

        assertThatThrownBy(() -> service.update(new UpdateThemeCommand(themeId, " Economy ")))
            .isInstanceOf(ThemeAlreadyExistsException.class);

        verify(themeRepository, never()).save(any());
    }

    @Test
    void shouldAllowUpdateWhenNormalizedNameMatchesSameTheme() {
        UUID themeId = UUID.randomUUID();
        Theme existingTheme = new Theme(themeId, "Politics");
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(existingTheme));
        when(themeRepository.findByNameIgnoreCase(" POLITICS ".trim())).thenReturn(Optional.of(existingTheme));
        when(themeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Theme result = service.update(new UpdateThemeCommand(themeId, " POLITICS "));

        assertThat(result).isEqualTo(new Theme(themeId, "POLITICS"));
        verify(themeRepository).findByNameIgnoreCase("POLITICS");
        verify(themeRepository).save(new Theme(themeId, "POLITICS"));
    }

    @Test
    void shouldRejectDeleteThemeWhenStillInUse() {
        UUID themeId = UUID.randomUUID();
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(new Theme(themeId, "Politics")));
        when(themeRepository.isInUse(themeId)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(themeId))
            .isInstanceOf(ThemeInUseException.class);

        verify(themeRepository, never()).deleteById(themeId);
    }

    @Test
    void shouldDeleteThemeWhenNotInUse() {
        UUID themeId = UUID.randomUUID();
        when(themeRepository.findById(themeId)).thenReturn(Optional.of(new Theme(themeId, "Politics")));
        when(themeRepository.isInUse(themeId)).thenReturn(false);

        service.delete(themeId);

        verify(themeRepository).deleteById(themeId);
    }
}
