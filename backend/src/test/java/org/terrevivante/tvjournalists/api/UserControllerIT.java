package org.terrevivante.tvjournalists.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.model.ApplicationUser;
import org.terrevivante.tvjournalists.domain.model.Role;
import org.terrevivante.tvjournalists.domain.port.ApplicationUserRepository;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
class UserControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateUserAsAdmin() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "username": "editor",
              "password": "editor123!",
              "firstName": "Edith",
              "lastName": "Editor",
              "enabled": true,
              "roles": ["USER"]
            }
            """;

        mockMvc.perform(post("/api/v1/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.username").value("editor"))
            .andExpect(jsonPath("$.firstName").value("Edith"))
            .andExpect(jsonPath("$.lastName").value("Editor"))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.roles", hasItem("USER")));
    }

    @Test
    void shouldReturnConflictWhenCreatingExistingUsername() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "username": "admin",
              "password": "another123!",
              "firstName": "Dup",
              "lastName": "Admin",
              "enabled": true,
              "roles": ["USER"]
            }
            """;

        mockMvc.perform(post("/api/v1/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectCreatingUserWhenEnabledIsMissing() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "username": "editor",
              "password": "editor123!",
              "firstName": "Edith",
              "lastName": "Editor",
              "roles": ["USER"]
            }
            """;

        mockMvc.perform(post("/api/v1/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("enabled"))
            .andExpect(jsonPath("$.errors[0].message").value("must not be null"));
    }

    @Test
    void shouldRejectCreatingUserWhenRolesAreEmpty() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "username": "editor",
              "password": "editor123!",
              "firstName": "Edith",
              "lastName": "Editor",
              "enabled": true,
              "roles": []
            }
            """;

        mockMvc.perform(post("/api/v1/users")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("roles"))
            .andExpect(jsonPath("$.errors[0].message").value("must not be empty"));
    }

    @Test
    void shouldForbidStandardUserFromAdminEndpoints() throws Exception {
        applicationUserRepository.save(new ApplicationUser(
            null,
            "standard-user",
            passwordEncoder.encode("standard123!"),
            "Standard",
            "User",
            true,
            Set.of(Role.USER)
        ));
        entityManager.flush();
        entityManager.clear();

        MockHttpSession session = loginAs("standard-user", "standard123!");

        mockMvc.perform(get("/api/v1/users").session(session))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownUser() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "firstName": "Missing",
              "lastName": "User",
              "enabled": true,
              "roles": ["USER"]
            }
            """;

        mockMvc.perform(put("/api/v1/users/{id}", UUID.randomUUID())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRejectUpdatingUserWhenEnabledIsMissing() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "firstName": "Admin",
              "lastName": "User",
              "roles": ["ADMIN", "USER"]
            }
            """;

        mockMvc.perform(put("/api/v1/users/{id}", bootstrapAdminId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("enabled"))
            .andExpect(jsonPath("$.errors[0].message").value("must not be null"));
    }

    @Test
    void shouldRejectUpdatingUserWhenRolesAreEmpty() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "firstName": "Admin",
              "lastName": "User",
              "enabled": true,
              "roles": []
            }
            """;

        mockMvc.perform(put("/api/v1/users/{id}", bootstrapAdminId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("roles"))
            .andExpect(jsonPath("$.errors[0].message").value("must not be empty"));
    }

    @Test
    void shouldRejectPasswordResetWhenPasswordIsTooShort() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");
        String requestBody = """
            {
              "password": "short"
            }
            """;

        mockMvc.perform(post("/api/v1/users/{id}/password-reset", bootstrapAdminId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("size must be between 8 and 2147483647"));
    }

    private UUID bootstrapAdminId() {
        return applicationUserRepository.findByUsername("admin")
            .map(ApplicationUser::id)
            .orElseThrow();
    }

    private MockHttpSession loginAs(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin("/api/login").user(username).password(password))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withUsername(username))
            .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
