package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
}
