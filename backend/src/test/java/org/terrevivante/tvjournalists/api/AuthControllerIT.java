package org.terrevivante.tvjournalists.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class AuthControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldReturnUnauthorizedWhenCurrentUserIsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCurrentUserPayloadFromDatabase() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");

        mockMvc.perform(get("/api/v1/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.firstName").value("Local"))
            .andExpect(jsonPath("$.lastName").value("Admin"))
            .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "USER")));
    }

    @Test
    void shouldReturnCurrentUserPayloadIncludingThemeManagerRole() throws Exception {
        entityManager.createNativeQuery("""
                INSERT INTO role (id, code, label)
                VALUES ('00000000-0000-0000-0000-000000000003', 'THEME_MANAGER', 'Gestionnaire des themes')
                ON CONFLICT (id) DO NOTHING
                """)
            .executeUpdate();
        entityManager.createNativeQuery("""
                INSERT INTO user_role (user_id, role_id)
                VALUES ('00000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000003')
                ON CONFLICT DO NOTHING
                """)
            .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        MockHttpSession session = loginAs("admin", "admin123!");

        mockMvc.perform(get("/api/v1/auth/me").session(session))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"))
            .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "THEME_MANAGER", "USER")));
    }

    private MockHttpSession loginAs(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin("/api/login").user(username).password(password))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withUsername(username))
            .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
