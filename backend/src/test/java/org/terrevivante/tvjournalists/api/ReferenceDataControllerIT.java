package org.terrevivante.tvjournalists.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;
import org.terrevivante.tvjournalists.domain.model.MediaType;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class ReferenceDataControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser
    void shouldListThemes() throws Exception {
        ThemeEntity theme = new ThemeEntity();
        theme.setName("Biodiversity");
        entityManager.persist(theme);
        entityManager.flush();

        mockMvc.perform(get("/api/v1/themes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].name", hasItem("Biodiversity")));
    }

    @Test
    @WithMockUser
    void shouldListMedia() throws Exception {
        MediaEntity media = new MediaEntity();
        media.setName("Green Press");
        media.setType(MediaType.PRESS);
        entityManager.persist(media);
        entityManager.flush();

        mockMvc.perform(get("/api/v1/media"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[*].name", hasItem("Green Press")))
            .andExpect(jsonPath("$[?(@.name == 'Green Press')].type", hasItem("PRESS")));
    }
}
