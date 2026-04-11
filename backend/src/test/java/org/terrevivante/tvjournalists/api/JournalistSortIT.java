package org.terrevivante.tvjournalists.api;

import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.domain.model.MediaType;
import org.terrevivante.tvjournalists.infrastructure.persistence.entity.MediaEntity;
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

/**
 * Verifies that the search API honours the {@code sort} parameter and returns
 * a Spring-Page-compatible response shape.
 *
 * <p>Fixtures use a unique media name ("SortTestMedia-…") so that the tests are
 * isolated from any other journalists created by other ITs sharing the same DB.
 */
@AutoConfigureMockMvc
@Transactional
class JournalistSortIT extends AbstractIntegrationTest {

    private static final String SORT_TEST_MEDIA = "SortTestMedia-Unique";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        MediaEntity sortMedia = fixtures.persistMedia(SORT_TEST_MEDIA, MediaType.PRESS);
        // Both journalists have the same unique media so we can filter to exactly these two.
        fixtures.persistJournalistWithActivity("Alice", "Green", sortMedia);
        fixtures.persistJournalistWithActivity("Bob", "Brown", sortMedia);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @WithMockUser
    void shouldSortByLastNameAscending() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", SORT_TEST_MEDIA)
                .param("sort", "lastName,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].lastName").value("Brown"))
            .andExpect(jsonPath("$.content[1].lastName").value("Green"));
    }

    @Test
    @WithMockUser
    void shouldSortByLastNameDescending() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", SORT_TEST_MEDIA)
                .param("sort", "lastName,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].lastName").value("Green"))
            .andExpect(jsonPath("$.content[1].lastName").value("Brown"));
    }

    @Test
    @WithMockUser
    void sortParamIsAcceptedWithoutError() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", SORT_TEST_MEDIA)
                .param("sort", "firstName,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @WithMockUser
    void shouldSupportMultipleSortParamsCompatibleWithSpringStyle() throws Exception {
        // Spring-style: sort=lastName,asc&sort=firstName,desc (repeated sort params)
        mockMvc.perform(get("/api/v1/journalists")
                .param("media", SORT_TEST_MEDIA)
                .param("sort", "lastName,asc")
                .param("sort", "firstName,desc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].lastName").value("Brown"))
            .andExpect(jsonPath("$.content[1].lastName").value("Green"));
    }

    @Test
    @WithMockUser
    void unknownSortField_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/journalists")
                .param("sort", "unknownField,asc"))
            .andExpect(status().isBadRequest());
    }
}
