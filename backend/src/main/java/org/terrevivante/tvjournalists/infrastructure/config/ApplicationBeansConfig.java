package org.terrevivante.tvjournalists.infrastructure.config;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.terrevivante.tvjournalists.application.service.InteractionApplicationService;
import org.terrevivante.tvjournalists.application.service.JournalistApplicationService;
import org.terrevivante.tvjournalists.application.service.ReferenceDataApplicationService;
import org.terrevivante.tvjournalists.application.service.UserApplicationService;
import org.terrevivante.tvjournalists.application.validation.ApplicationValidator;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public ApplicationValidator applicationValidator(Validator validator) {
        return new ApplicationValidator(validator);
    }

    @Bean
    public JournalistApplicationService journalistApplicationService(
            JournalistRepository journalistRepository,
            ApplicationValidator applicationValidator) {
        return new JournalistApplicationService(journalistRepository, applicationValidator);
    }

    @Bean
    public InteractionApplicationService interactionApplicationService(
            InteractionLogRepository interactionLogRepository,
            JournalistRepository journalistRepository,
            ActivityRepository activityRepository,
            ApplicationValidator applicationValidator) {
        return new InteractionApplicationService(
            interactionLogRepository,
            journalistRepository,
            activityRepository,
            applicationValidator
        );
    }

    @Bean
    public UserApplicationService userApplicationService(
            ApplicationUserRepository applicationUserRepository,
            PasswordEncoder passwordEncoder,
            ApplicationValidator applicationValidator) {
        return new UserApplicationService(applicationUserRepository, passwordEncoder, applicationValidator);
    }

    @Bean
    public ReferenceDataApplicationService referenceDataApplicationService(
            MediaRepository mediaRepository,
            ThemeRepository themeRepository) {
        return new ReferenceDataApplicationService(mediaRepository, themeRepository);
    }
}
