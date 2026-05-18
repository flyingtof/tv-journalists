package org.terrevivante.tvjournalists.api;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ActivityEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.JournalistEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.ThemeEntity;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ThemeControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldCreateThemeAsThemeManager() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        mockMvc.perform(post("/api/v1/themes")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "  Biodiversity  "
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.name").value("Biodiversity"));
    }

    @Test
    void shouldReturnConflictWhenCreatingThemeWithDuplicateName() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        ThemeEntity existingTheme = new ThemeEntity();
        existingTheme.setName("Climate");
        entityManager.persist(existingTheme);
        entityManager.flush();

        mockMvc.perform(post("/api/v1/themes")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "  climate  "
                    }
                    """))
            .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(ThemeEntity.class, existingTheme.getId()).getName()).isEqualTo("Climate");
    }

    @Test
    void shouldUpdateThemeAsThemeManager() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        ThemeEntity theme = new ThemeEntity();
        theme.setName("Climate");
        entityManager.persist(theme);
        entityManager.flush();

        mockMvc.perform(put("/api/v1/themes/{id}", theme.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "  Biodiversity  "
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(theme.getId().toString()))
            .andExpect(jsonPath("$.name").value("Biodiversity"));

        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(ThemeEntity.class, theme.getId()).getName()).isEqualTo("Biodiversity");
    }

    @Test
    void shouldDeleteThemeAsThemeManager() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        ThemeEntity theme = new ThemeEntity();
        theme.setName("Climate");
        entityManager.persist(theme);
        entityManager.flush();

        mockMvc.perform(delete("/api/v1/themes/{id}", theme.getId()).session(session))
            .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();
        assertThat(entityManager.find(ThemeEntity.class, theme.getId())).isNull();
    }

    @Test
    void shouldReturnConflictWhenUpdatingThemeWithDuplicateName() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        ThemeEntity climate = new ThemeEntity();
        climate.setName("Climate");
        entityManager.persist(climate);

        ThemeEntity biodiversity = new ThemeEntity();
        biodiversity.setName("Biodiversity");
        entityManager.persist(biodiversity);
        entityManager.flush();

        mockMvc.perform(put("/api/v1/themes/{id}", biodiversity.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "  climate  "
                    }
                    """))
            .andExpect(status().isConflict());

        entityManager.clear();
        assertThat(entityManager.find(ThemeEntity.class, biodiversity.getId()).getName()).isEqualTo("Biodiversity");
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingUnknownTheme() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        mockMvc.perform(put("/api/v1/themes/{id}", UUID.randomUUID())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Biodiversity"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownTheme() throws Exception {
        createThemeManagerUser();
        MockHttpSession session = loginAs("theme-manager", "theme123!");

        mockMvc.perform(delete("/api/v1/themes/{id}", UUID.randomUUID()).session(session))
            .andExpect(status().isNotFound());
    }

    @Test
    void shouldRefuseDeletingThemeWhenUsedByActivity() throws Exception {
        MockHttpSession session = loginAs("admin", "admin123!");

        ThemeEntity theme = new ThemeEntity();
        theme.setName("Climate");
        entityManager.persist(theme);

        MediaEntity media = new MediaEntity();
        media.setName("Green Press");
        media.setType(org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        entityManager.persist(media);

        JournalistEntity journalist = new JournalistEntity("Alice", "Durand");
        entityManager.persist(journalist);

        ActivityEntity activity = new ActivityEntity();
        activity.setJournalist(journalist);
        activity.setMedia(media);
        activity.setRole("Reporter");
        activity.getThemes().add(theme);
        entityManager.persist(activity);
        entityManager.flush();

        mockMvc.perform(delete("/api/v1/themes/{id}", theme.getId()).session(session))
            .andExpect(status().isConflict());
        entityManager.clear();
        assertThat(entityManager.find(ThemeEntity.class, theme.getId())).isNotNull();
    }

    @Test
    void shouldForbidStandardUserOnThemeWriteEndpoints() throws Exception {
        createUser("standard-user", "standard123!", Set.of(Role.USER));
        MockHttpSession session = loginAs("standard-user", "standard123!");

        ThemeEntity theme = new ThemeEntity();
        theme.setName("Climate");
        entityManager.persist(theme);
        entityManager.flush();

        mockMvc.perform(post("/api/v1/themes")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Biodiversity"
                    }
                    """))
            .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/themes/{id}", theme.getId())
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Environment"
                    }
                    """))
            .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/themes/{id}", theme.getId()).session(session))
            .andExpect(status().isForbidden());
    }

    private void createThemeManagerUser() {
        createUser("theme-manager", "theme123!", Set.of(Role.THEME_MANAGER));
    }

    private void createUser(String username, String password, Set<Role> roles) {
        applicationUserRepository.save(new ApplicationUser(
            null,
            username,
            passwordEncoder.encode(password),
            "Theo",
            "Manager",
            true,
            roles
        ));
        entityManager.flush();
        entityManager.clear();
    }

    private MockHttpSession loginAs(String username, String password) throws Exception {
        MvcResult loginResult = mockMvc.perform(formLogin("/api/login").user(username).password(password))
            .andExpect(status().is3xxRedirection())
            .andExpect(authenticated().withUsername(username))
            .andReturn();

        return (MockHttpSession) loginResult.getRequest().getSession(false);
    }
}
