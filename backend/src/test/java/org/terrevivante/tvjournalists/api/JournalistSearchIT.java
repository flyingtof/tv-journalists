package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;
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
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);

        ThemeEntity biodiversity = fixtures.persistTheme("Biodiversity");
        MediaEntity press = fixtures.persistMedia("Green Press", MediaType.PRESS);
        fixtures.persistJournalistWithActivity("Alice", "Green", press, biodiversity);
        fixtures.persistJournalist("Bob", "Brown");

        entityManager.flush();
        entityManager.clear();
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

    @Test
    @WithMockUser
    void responseHasSpringPageCompatibleShape() throws Exception {
        mockMvc.perform(get("/api/v1/journalists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.totalPages").isNumber())
            .andExpect(jsonPath("$.number").isNumber())
            .andExpect(jsonPath("$.size").isNumber())
            .andExpect(jsonPath("$.totalElements").isNumber())
            .andExpect(jsonPath("$.first").isBoolean())
            .andExpect(jsonPath("$.last").isBoolean())
            .andExpect(jsonPath("$.numberOfElements").isNumber())
            .andExpect(jsonPath("$.empty").isBoolean());
    }

    @Test
    @WithMockUser
    void combinedMediaAndThemeFilter_doesNotProduceDuplicateJournalists() throws Exception {
        // Alice already has activity: Green Press + Biodiversity (from setUp).
        // Add a second activity for Alice with a different media but the same theme.
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        MediaEntity secondMedia = fixtures.persistMedia("Blue Radio");
        ThemeEntity biodiversity = entityManager
                .createQuery("select t from ThemeEntity t where t.name = 'Biodiversity'",
                        org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity.class)
                .getSingleResult();
        org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity alice = entityManager
                .createQuery("select j from JournalistEntity j where j.firstName = 'Alice'",
                        org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity.class)
                .getSingleResult();

        org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity second =
                new org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity();
        second.setJournalist(alice);
        second.setMedia(secondMedia);
        second.getThemes().add(biodiversity);
        entityManager.persist(second);
        entityManager.flush();
        entityManager.clear();

        // Filtering by Green Press AND Biodiversity should return exactly ONE journalist
        // (Alice), not two — even though Alice now has two activities matching themes.
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", "Green Press")
                .param("themes", "Biodiversity"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.content[0].firstName").value("Alice"));
    }
}
