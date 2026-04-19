package org.terrevivante.tvjournalists.application.validation;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public class ApplicationValidator {

    private final Validator validator;

    public ApplicationValidator(Validator validator) {
        this.validator = validator;
    }

    public <T> void validate(T object) {
        var violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
