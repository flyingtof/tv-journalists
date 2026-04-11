package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.Journalist;
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
    void shouldLogInteraction() throws Exception {
        Journalist journalist = new Journalist("John", "Doe");
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
            .andExpect(status().isCreated());
    }
}
