package org.terrevivante.tvjournalists.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapThemeIntegrityViolationToConflict() {
        assertThat(handler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("violates unique constraint \"ux_theme_name_lower\"")))
            .extracting(response -> response.getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldMapThemeReferenceViolationToConflict() {
        assertThat(handler.handleDataIntegrityViolation(
            new DataIntegrityViolationException("violates foreign key constraint \"activity_themes_theme_id_fkey\"")))
            .extracting(response -> response.getStatusCode())
            .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldNotConvertUnrelatedDataIntegrityViolationToConflict() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("constraint on another table");

        assertThatThrownBy(() -> handler.handleDataIntegrityViolation(exception))
            .isSameAs(exception);
    }
}
