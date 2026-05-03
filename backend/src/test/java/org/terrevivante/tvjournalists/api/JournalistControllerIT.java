package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
public class JournalistControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenJournalistDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/journalists/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldCreateJournalist() throws Exception {
        String journalistJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "globalEmail": "john.doe@example.com"
            }
            """;

        mockMvc.perform(post("/api/v1/journalists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(journalistJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.globalEmail").value("john.doe@example.com"))
            .andExpect(jsonPath("$.activities").isArray());
    }

    @Test
    @WithMockUser
    void shouldReturnStructuredValidationErrorsWhenJournalistEmailIsInvalid() throws Exception {
        String journalistJson = """
            {
                "firstName": "John",
                "lastName": "Doe",
                "globalEmail": "not-an-email"
            }
            """;

        mockMvc.perform(post("/api/v1/journalists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(journalistJson))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("globalEmail"))
            .andExpect(jsonPath("$.errors[0].message").value("must be a well-formed email address"));
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("page", "-1"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenSortFieldIsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("sort", ",asc"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturnProfilePayloadWithoutUnusedActivityFields() throws Exception {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var theme = fixtures.persistTheme("Biodiversity");
        var media = fixtures.persistMedia("Green Press", org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        var journalist = fixtures.persistJournalistWithActivity("Alice", "Green", media, theme);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/v1/journalists/" + journalist.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Alice"))
            .andExpect(jsonPath("$.activities[0].mediaName").value("Green Press"))
            .andExpect(jsonPath("$.activities[0].themes[0].name").value("Biodiversity"))
            .andExpect(jsonPath("$.activities[0].mediaId").doesNotExist())
            .andExpect(jsonPath("$.activities[0].specificPhone").doesNotExist());
    }
}
