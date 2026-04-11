package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.Journalist;
import org.terrevivante.tvjournalists.domain.Media;
import org.terrevivante.tvjournalists.domain.Theme;
import org.terrevivante.tvjournalists.domain.Activity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class JournalistSearchIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        Theme biodiversity = new Theme();
        biodiversity.setName("Biodiversity");
        entityManager.persist(biodiversity);

        Media press = new Media();
        press.setName("Green Press");
        press.setType(Media.MediaType.PRESS);
        entityManager.persist(press);

        Journalist j1 = new Journalist("Alice", "Green");
        entityManager.persist(j1);

        Activity a1 = new Activity();
        a1.setJournalist(j1);
        a1.setMedia(press);
        a1.getThemes().add(biodiversity);
        j1.getActivities().add(a1); // Bidirectional consistency
        entityManager.persist(a1);

        Journalist j2 = new Journalist("Bob", "Brown");
        entityManager.persist(j2);
        
        entityManager.flush();
        entityManager.clear(); // Clear to force reload from DB
    }

    @Test
    @WithMockUser
    void shouldSearchByTheme() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("themes", "Biodiversity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }

    @Test
    @WithMockUser
    void shouldSearchByMedia() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", "Green Press"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }
}
