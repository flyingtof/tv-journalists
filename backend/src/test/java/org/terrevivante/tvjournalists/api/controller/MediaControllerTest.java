package org.terrevivante.tvjournalists.api.controller;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.api.dto.MediaDTO;
import org.terrevivante.tvjournalists.application.usecase.ListMediaUseCase;
import org.terrevivante.tvjournalists.domain.model.Media;
import org.terrevivante.tvjournalists.domain.model.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MediaControllerTest {

    private final ListMediaUseCase listMediaUseCase = mock(ListMediaUseCase.class);
    private final MediaController controller = new MediaController(listMediaUseCase);

    @Test
    void getAllMedia_delegatesToUseCase() {
        Media media = new Media(UUID.randomUUID(), "Green Press", MediaType.PRESS, null);
        when(listMediaUseCase.listMedia()).thenReturn(List.of(media));

        List<MediaDTO> result = controller.getAllMedia();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Green Press");
        assertThat(result.get(0).type()).isEqualTo(MediaType.PRESS);
        verify(listMediaUseCase).listMedia();
    }
}
