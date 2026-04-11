package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class InteractionControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser
    void shouldReturn409WhenActivityBelongsToAnotherJournalist() throws Exception {
        MediaEntity media = new MediaEntity();
        media.setName("Some TV Channel");
        media.setType(org.terrevivante.tvjournalists.domain.model.MediaType.TV);
        entityManager.persist(media);

        JournalistEntity owner = new JournalistEntity("Jane", "Smith");
        entityManager.persist(owner);

        ActivityEntity activity = new ActivityEntity();
        activity.setJournalist(owner);
        activity.setMedia(media);
        entityManager.persist(activity);

        JournalistEntity other = new JournalistEntity("Bob", "Jones");
        entityManager.persist(other);

        entityManager.flush();

        String body = """
            {
                "date": "2026-03-29",
                "description": "Conference",
                "activityId": "%s"
            }
            """.formatted(activity.getId());

        mockMvc.perform(post("/api/v1/journalists/" + other.getId() + "/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInteractionDescriptionIsBlank() throws Exception {
        JournalistEntity journalist = new JournalistEntity("John", "Doe");
        entityManager.persist(journalist);
        entityManager.flush();

        String body = """
            {
                "date": "2026-03-29",
                "description": ""
            }
            """;

        mockMvc.perform(post("/api/v1/journalists/" + journalist.getId() + "/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInteractionDescriptionIsWhitespace() throws Exception {
        JournalistEntity journalist = new JournalistEntity("John", "Doe");
        entityManager.persist(journalist);
        entityManager.flush();

        String body = """
            {
                "date": "2026-03-29",
                "description": "   "
            }
            """;

        mockMvc.perform(post("/api/v1/journalists/" + journalist.getId() + "/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenInteractionDateIsInFuture() throws Exception {
        JournalistEntity journalist = new JournalistEntity("John", "Doe");
        entityManager.persist(journalist);
        entityManager.flush();

        String body = """
            {
                "date": "2099-01-01",
                "description": "Future event"
            }
            """;

        mockMvc.perform(post("/api/v1/journalists/" + journalist.getId() + "/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldLogInteraction() throws Exception {
        JournalistEntity journalist = new JournalistEntity("John", "Doe");
        entityManager.persist(journalist);
        entityManager.flush();

        String interactionJson = """
            {
                "date": "2026-03-29",
                "description": "Met at conference"
            }
            """;

        mockMvc.perform(post("/api/v1/journalists/" + journalist.getId() + "/interactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(interactionJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.date").value("2026-03-29"))
            .andExpect(jsonPath("$.description").value("Met at conference"))
            .andExpect(jsonPath("$.activityId").value((Object) null));
    }
}
