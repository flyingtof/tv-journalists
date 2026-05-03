package org.terrevivante.tvjournalists.application.service;

import org.junit.jupiter.api.Test;
import org.terrevivante.tvjournalists.application.exception.JournalistNotFoundException;
import org.terrevivante.tvjournalists.application.port.JournalistReadRepository;
import org.terrevivante.tvjournalists.application.readmodel.JournalistListItemView;
import org.terrevivante.tvjournalists.application.readmodel.JournalistProfileView;
import org.terrevivante.tvjournalists.domain.query.JournalistSearchCriteria;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JournalistReadApplicationServiceTest {

    private final JournalistReadRepository journalistReadRepository = mock(JournalistReadRepository.class);
    private final JournalistReadApplicationService service = new JournalistReadApplicationService(journalistReadRepository);

    @Test
    void search_delegatesToReadRepository() {
        JournalistSearchCriteria criteria = new JournalistSearchCriteria("ali", List.of(), List.of());
        PageRequest pageRequest = new PageRequest(0, 20);
        PageResult<JournalistListItemView> expected = new PageResult<>(List.of(), 0, 0, 20);
        when(journalistReadRepository.search(eq(criteria), eq(pageRequest))).thenReturn(expected);

        PageResult<JournalistListItemView> result = service.search(criteria, pageRequest);

        assertThat(result).isSameAs(expected);
        verify(journalistReadRepository).search(eq(criteria), eq(pageRequest));
    }

    @Test
    void getById_returnsProfileWhenFound() {
        UUID journalistId = UUID.randomUUID();
        JournalistProfileView profile = new JournalistProfileView(
            journalistId,
            "Alice",
            "Green",
            "alice@example.com",
            "+33600000000",
            List.of()
        );
        when(journalistReadRepository.findProfileById(journalistId)).thenReturn(Optional.of(profile));

        JournalistProfileView result = service.getById(journalistId);

        assertThat(result).isEqualTo(profile);
    }

    @Test
    void getById_throwsWhenProfileIsMissing() {
        UUID journalistId = UUID.randomUUID();
        when(journalistReadRepository.findProfileById(journalistId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(journalistId))
            .isInstanceOf(JournalistNotFoundException.class);
    }
}

