package org.terrevivante.tvjournalists.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.terrevivante.tvjournalists.application.service.InteractionApplicationService;
import org.terrevivante.tvjournalists.application.service.JournalistApplicationService;
import org.terrevivante.tvjournalists.application.service.ReferenceDataApplicationService;
import org.terrevivante.tvjournalists.domain.port.ActivityRepository;
import org.terrevivante.tvjournalists.domain.port.InteractionLogRepository;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;
import org.terrevivante.tvjournalists.domain.port.ThemeRepository;

@Configuration
public class ApplicationBeansConfig {

    @Bean
    public JournalistApplicationService journalistApplicationService(JournalistRepository journalistRepository) {
        return new JournalistApplicationService(journalistRepository);
    }

    @Bean
    public InteractionApplicationService interactionApplicationService(
            InteractionLogRepository interactionLogRepository,
            JournalistRepository journalistRepository,
            ActivityRepository activityRepository) {
        return new InteractionApplicationService(interactionLogRepository, journalistRepository, activityRepository);
    }

    @Bean
    public ReferenceDataApplicationService referenceDataApplicationService(
            MediaRepository mediaRepository,
            ThemeRepository themeRepository) {
        return new ReferenceDataApplicationService(mediaRepository, themeRepository);
    }
}
