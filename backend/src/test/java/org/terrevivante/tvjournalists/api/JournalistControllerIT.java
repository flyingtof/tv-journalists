package org.terrevivante.tvjournalists.api;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class JournalistControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldAllowJournalistManagerToCreateJournalist() throws Exception {
        mockMvc.perform(post("/api/v1/journalists")
                .with(user("manager").roles("JOURNALIST_MANAGER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName": "Alice",
                      "lastName": "Martin",
                      "globalEmail": "alice@example.com",
                      "globalPhone": "+33123456789",
                      "activities": []
                    }
                    """))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser
    void shouldReturnNotFoundWhenJournalistDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/v1/journalists/" + UUID.randomUUID()))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "JOURNALIST_MANAGER")
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
    @WithMockUser(roles = "JOURNALIST_MANAGER")
    void shouldCreateJournalistWithActivities() throws Exception {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var theme = fixtures.persistTheme("Biodiversity");
        var media = fixtures.persistMedia("Green Press", org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/journalists")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "globalEmail": "john.doe@example.com",
                        "activities": [{
                            "mediaId": "%s",
                            "specificEmail": "john@press.com",
                            "specificPhone": "+33111111111",
                            "themeIds": ["%s"]
                        }]
                    }
                    """.formatted(media.getId(), theme.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.activities").isArray())
            .andExpect(jsonPath("$.activities[0].mediaName").value("Green Press"))
            .andExpect(jsonPath("$.activities[0].specificEmail").value("john@press.com"))
            .andExpect(jsonPath("$.activities[0].themes[0].name").value("Biodiversity"));
    }

    @Test
    @WithMockUser(roles = "JOURNALIST_MANAGER")
    void shouldUpdateJournalist() throws Exception {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var theme = fixtures.persistTheme("Biodiversity");
        var media = fixtures.persistMedia("Green Press", org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        var journalist = fixtures.persistJournalist("John", "Doe");
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(put("/api/v1/journalists/" + journalist.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "Jane",
                        "lastName": "Doe",
                        "globalEmail": "jane.doe@example.com",
                        "activities": [{
                            "mediaId": "%s",
                            "themeIds": ["%s"]
                        }]
                    }
                    """.formatted(media.getId(), theme.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Jane"))
            .andExpect(jsonPath("$.activities[0].mediaName").value("Green Press"));
    }

    @Test
    @WithMockUser(roles = "JOURNALIST_MANAGER")
    void shouldDeleteJournalistAndItsInteractions() throws Exception {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var theme = fixtures.persistTheme("Biodiversity");
        var media = fixtures.persistMedia("Green Press", org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        var journalist = fixtures.persistJournalistWithActivity("John", "Doe", media, theme);
        entityManager.flush();
        entityManager.clear();

        UUID activityId = entityManager.createQuery(
                "select a.id from ActivityEntity a where a.journalist.id = :journalistId", UUID.class)
            .setParameter("journalistId", journalist.getId())
            .getSingleResult();

        entityManager.createNativeQuery("""
            insert into interaction_log (id, journalist_id, activity_id, date, description, created_at)
            values (?, ?, ?, ?, ?, now())
            """)
            .setParameter(1, UUID.randomUUID())
            .setParameter(2, journalist.getId())
            .setParameter(3, activityId)
            .setParameter(4, LocalDate.now())
            .setParameter(5, "Test interaction")
            .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(delete("/api/v1/journalists/" + journalist.getId()))
            .andExpect(status().isNoContent());

        Long remainingJournalists = entityManager.createQuery(
                "select count(j) from JournalistEntity j where j.id = :id", Long.class)
            .setParameter("id", journalist.getId())
            .getSingleResult();
        Long remainingInteractions = entityManager.createQuery(
                "select count(l) from InteractionLogEntity l where l.journalistId = :id", Long.class)
            .setParameter("id", journalist.getId())
            .getSingleResult();

        assertThat(remainingJournalists).isZero();
        assertThat(remainingInteractions).isZero();
    }

    @Test
    @WithMockUser(roles = "JOURNALIST_MANAGER")
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
    void shouldForbidPlainUserFromCreatingJournalist() throws Exception {
        mockMvc.perform(post("/api/v1/journalists")
                .with(user("plain-user").roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "firstName": "Bob",
                      "lastName": "Smith",
                      "globalEmail": "bob@example.com"
                    }
                    """))
            .andExpect(status().isForbidden());
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
        var journalist = fixtures.persistJournalistWithActivity("Alice", "Green", media, "+33600000000", theme);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(get("/api/v1/journalists/" + journalist.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("Alice"))
            .andExpect(jsonPath("$.activities[0].mediaName").value("Green Press"))
            .andExpect(jsonPath("$.activities[0].themes[0].name").value("Biodiversity"))
            .andExpect(jsonPath("$.activities[0].mediaId").doesNotExist())
            .andExpect(jsonPath("$.activities[0].specificPhone").value("+33600000000"));
    }

    @Test
    @WithMockUser(roles = "JOURNALIST_MANAGER")
    void shouldCreateJournalistWithActivityWithoutOptionalEmailAndPhone() throws Exception {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var theme = fixtures.persistTheme("Biodiversity");
        var media = fixtures.persistMedia("Green Press", org.terrevivante.tvjournalists.domain.model.MediaType.PRESS);
        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/journalists")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "firstName": "John",
                        "lastName": "Doe",
                        "activities": [{
                            "mediaId": "%s",
                            "themeIds": ["%s"]
                        }]
                    }
                    """.formatted(media.getId(), theme.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.activities").isArray())
            .andExpect(jsonPath("$.activities[0].mediaName").value("Green Press"))
            .andExpect(jsonPath("$.activities[0].specificEmail").doesNotExist())
            .andExpect(jsonPath("$.activities[0].specificPhone").doesNotExist());
    }
}
