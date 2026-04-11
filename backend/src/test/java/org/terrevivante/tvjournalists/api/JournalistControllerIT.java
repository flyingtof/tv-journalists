package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class JournalistControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
            .andExpect(status().isCreated());
    }
}
