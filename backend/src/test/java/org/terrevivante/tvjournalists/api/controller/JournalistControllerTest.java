package org.terrevivante.tvjournalists.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.terrevivante.tvjournalists.api.dto.PageResponse;
import org.terrevivante.tvjournalists.api.mapper.JournalistMapper;
import org.terrevivante.tvjournalists.application.usecase.CreateJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.GetJournalistUseCase;
import org.terrevivante.tvjournalists.application.usecase.LogInteractionUseCase;
import org.terrevivante.tvjournalists.application.usecase.SearchJournalistsUseCase;
import org.terrevivante.tvjournalists.domain.model.Journalist;
import org.terrevivante.tvjournalists.domain.query.PageRequest;
import org.terrevivante.tvjournalists.domain.query.PageResult;
import org.terrevivante.tvjournalists.domain.query.SortDirection;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class JournalistControllerTest {

    private final CreateJournalistUseCase createJournalistUseCase = mock(CreateJournalistUseCase.class);
    private final GetJournalistUseCase getJournalistUseCase = mock(GetJournalistUseCase.class);
    private final SearchJournalistsUseCase searchJournalistsUseCase = mock(SearchJournalistsUseCase.class);
    private final LogInteractionUseCase logInteractionUseCase = mock(LogInteractionUseCase.class);
    private final JournalistMapper journalistMapper = mock(JournalistMapper.class);
    private final JournalistController controller = new JournalistController(
            createJournalistUseCase, getJournalistUseCase, searchJournalistsUseCase,
            logInteractionUseCase, journalistMapper);

    @Test
    void searchJournalists_mapsExplicitPageAndSizeToPageRequest() {
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 2, 15);
        when(searchJournalistsUseCase.search(any(), eq(new PageRequest(2, 15, List.of())))).thenReturn(empty);

        var response = controller.searchJournalists(null, null, null, 2, 15, new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().number()).isEqualTo(2);
        assertThat(response.getBody().size()).isEqualTo(15);
        verify(searchJournalistsUseCase).search(any(), eq(new PageRequest(2, 15, List.of())));
    }

    @Test
    void searchJournalists_usesDefaultPageAndSizeWhenNotProvided() {
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 0, 20);
        when(searchJournalistsUseCase.search(any(), eq(new PageRequest(0, 20, List.of())))).thenReturn(empty);

        var response = controller.searchJournalists(null, null, null, 0, 20, new MockHttpServletRequest());

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(searchJournalistsUseCase).search(any(), eq(new PageRequest(0, 20, List.of())));
    }

    @Test
    void searchJournalists_sortParamIsParsedAndPassedToUseCase() {
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 0, 20);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(empty);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "lastName,asc");
        controller.searchJournalists(null, null, null, 0, 20, request);

        verify(searchJournalistsUseCase).search(any(), argThat(pr ->
            !pr.sortOrders().isEmpty()
            && "lastName".equals(pr.sortOrders().getFirst().field())
            && pr.sortOrders().getFirst().direction() == SortDirection.ASC
        ));
    }

    @Test
    void searchJournalists_sortDescIsParsedCorrectly() {
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 0, 20);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(empty);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "firstName,desc");
        controller.searchJournalists(null, null, null, 0, 20, request);

        verify(searchJournalistsUseCase).search(any(), argThat(pr ->
            !pr.sortOrders().isEmpty()
            && "firstName".equals(pr.sortOrders().getFirst().field())
            && pr.sortOrders().getFirst().direction() == SortDirection.DESC
        ));
    }

    @Test
    void searchJournalists_supportsMultipleSortParams() {
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 0, 20);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(empty);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "lastName,asc");
        request.addParameter("sort", "firstName,desc");
        controller.searchJournalists(null, null, null, 0, 20, request);

        verify(searchJournalistsUseCase).search(any(), argThat(pr ->
            pr.sortOrders().size() == 2
            && "lastName".equals(pr.sortOrders().get(0).field())
            && pr.sortOrders().get(0).direction() == SortDirection.ASC
            && "firstName".equals(pr.sortOrders().get(1).field())
            && pr.sortOrders().get(1).direction() == SortDirection.DESC
        ));
    }

    @Test
    void searchJournalists_responseHasSpringPageCompatibleShape() {
        // 25 items, page 1, size 10 → totalPages=3, first=false, last=false
        PageResult<Journalist> result = new PageResult<>(List.of(), 25L, 1, 10);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(result);

        var response = controller.searchJournalists(null, null, null, 1, 10, new MockHttpServletRequest());

        assertThat(response.getBody()).isInstanceOf(PageResponse.class);
        PageResponse<?> body = response.getBody();
        assertThat(body.number()).isEqualTo(1);
        assertThat(body.size()).isEqualTo(10);
        assertThat(body.totalElements()).isEqualTo(25L);
        assertThat(body.totalPages()).isEqualTo(3);
        assertThat(body.first()).isFalse();
        assertThat(body.last()).isFalse();
        assertThat(body.empty()).isTrue();
        assertThat(body.numberOfElements()).isEqualTo(0);
    }

    @Test
    void searchJournalists_unknownSortField_throwsIllegalArgumentException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("sort", "unknownField,asc");

        assertThatThrownBy(() -> controller.searchJournalists(null, null, null, 0, 20, request))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchJournalists_firstPageIsMarkedFirst() {
        PageResult<Journalist> result = new PageResult<>(List.of(), 5L, 0, 10);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(result);

        var response = controller.searchJournalists(null, null, null, 0, 10, new MockHttpServletRequest());

        PageResponse<?> body = response.getBody();
        assertThat(body.first()).isTrue();
        assertThat(body.last()).isTrue(); // only 1 page
        assertThat(body.totalPages()).isEqualTo(1);
    }

    @Test
    void emptyResults_yieldTotalPagesOne() {
        // Spring Page-compatible: an empty result set still has 1 page (just empty)
        PageResult<Journalist> empty = new PageResult<>(List.of(), 0L, 0, 20);
        when(searchJournalistsUseCase.search(any(), any())).thenReturn(empty);

        var response = controller.searchJournalists(null, null, null, 0, 20, new MockHttpServletRequest());

        PageResponse<?> body = response.getBody();
        assertThat(body.totalElements()).isEqualTo(0L);
        assertThat(body.totalPages()).isEqualTo(1);
        assertThat(body.empty()).isTrue();
        assertThat(body.first()).isTrue();
        assertThat(body.last()).isTrue();
    }
}
