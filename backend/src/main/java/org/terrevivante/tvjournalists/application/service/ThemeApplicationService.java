package org.terrevivante.tvjournalists.application.service;

import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.application.command.CreateThemeCommand;
import org.terrevivante.tvjournalists.application.command.UpdateThemeCommand;
import org.terrevivante.tvjournalists.application.exception.ThemeAlreadyExistsException;
import org.terrevivante.tvjournalists.application.exception.ThemeInUseException;
import org.terrevivante.tvjournalists.application.exception.ThemeNotFoundException;
import org.terrevivante.tvjournalists.application.usecase.CreateThemeUseCase;
import org.terrevivante.tvjournalists.application.usecase.DeleteThemeUseCase;
import org.terrevivante.tvjournalists.application.usecase.ListThemesUseCase;
import org.terrevivante.tvjournalists.application.usecase.UpdateThemeUseCase;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.model.Theme;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
public class ThemeApplicationService
    implements CreateThemeUseCase, UpdateThemeUseCase, DeleteThemeUseCase, ListThemesUseCase {

    private final ThemeRepository themeRepository;
    private final ApplicationValidator applicationValidator;

    public ThemeApplicationService(ThemeRepository themeRepository, ApplicationValidator applicationValidator) {
        this.themeRepository = themeRepository;
        this.applicationValidator = applicationValidator;
    }

    @Override
    @Transactional
    public Theme create(CreateThemeCommand command) {
        applicationValidator.validate(command);
        String trimmedName = trimName(command.name());

        ensureUniqueName(trimmedName, null);

        return themeRepository.save(new Theme(null, trimmedName));
    }

    @Override
    @Transactional
    public Theme update(UpdateThemeCommand command) {
        applicationValidator.validate(command);
        Theme existingTheme = getById(command.id());
        String trimmedName = trimName(command.name());

        ensureUniqueName(trimmedName, existingTheme.id());

        return themeRepository.save(new Theme(existingTheme.id(), trimmedName));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Theme theme = getById(id);

        if (themeRepository.isInUse(theme.id())) {
            throw new ThemeInUseException(theme.id());
        }

        themeRepository.deleteById(theme.id());
    }

    @Override
    public List<Theme> listThemes() {
        return themeRepository.findAll();
    }

    private Theme getById(UUID id) {
        return themeRepository.findById(id)
            .orElseThrow(() -> new ThemeNotFoundException(id));
    }

    private void ensureUniqueName(String trimmedName, UUID currentThemeId) {
        themeRepository.findByNameIgnoreCase(trimmedName)
            .filter(existingTheme -> !existingTheme.id().equals(currentThemeId))
            .ifPresent(existingTheme -> {
                throw new ThemeAlreadyExistsException(trimmedName);
            });
    }

    private String trimName(String name) {
        return name.trim();
    }
}
