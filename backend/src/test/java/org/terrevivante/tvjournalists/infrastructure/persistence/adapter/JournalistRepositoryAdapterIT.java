package org.terrevivante.tvjournalists.infrastructure.persistence.adapter;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.terrevivante.tvjournalists.AbstractIntegrationTest;
import org.terrevivante.tvjournalists.api.JournalistFixtures;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.port.JournalistRepository;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class JournalistRepositoryAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private JournalistRepository journalistRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        JournalistFixtures fixtures = new JournalistFixtures(entityManager);
        var biodiversity = fixtures.persistTheme("Biodiversity");
        var greenPress = fixtures.persistMedia("Green Press");
        fixtures.persistJournalistWithActivity("Alice", "Green", greenPress, biodiversity);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnCoreJournalistsFromJpaAdapterSearch() {
        PageResult<Journalist> result = journalistRepository.search(
            new JournalistSearchCriteria("Alice", List.of("Green Press"), List.of("Biodiversity")),
            new PageRequest(0, 20)
        );

        assertThat(result.content()).extracting(Journalist::firstName).contains("Alice");
        assertThat(result.content().getFirst().activities()).isNotEmpty();
    }
}
