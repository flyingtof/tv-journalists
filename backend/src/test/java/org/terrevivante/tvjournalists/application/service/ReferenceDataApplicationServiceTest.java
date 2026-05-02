package org.terrevivante.tvjournalists.application.service;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.domain.port.MediaRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReferenceDataApplicationServiceTest {

    private final MediaRepository mediaRepository = mock(MediaRepository.class);
    private final ReferenceDataApplicationService service = new ReferenceDataApplicationService(mediaRepository);

    @Test
    void shouldListAllMedia() {
        Media media = new Media(UUID.randomUUID(), "Le Monde", MediaType.PRESS, "https://lemonde.fr");
        when(mediaRepository.findAll()).thenReturn(List.of(media));

        List<Media> result = service.listMedia();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Le Monde");
    }
}
