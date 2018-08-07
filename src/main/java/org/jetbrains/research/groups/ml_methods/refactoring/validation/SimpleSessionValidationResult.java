package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Dummy implementation of @link{SessionValidationResult}. Just stores correct and incorrect values */
public class SimpleSessionValidationResult implements SessionValidationResult {
    private List<String> errors = new ArrayList<>();
    private List<String> valid = new ArrayList<>();

    public List<String> errorLines() {
        return errors;
    }

    public List<String> validLines() {
        return valid;
    }

    @Override
    public void addErrorSession(List<EventLine> errorSession) {
        errors.addAll(errorSession.stream().map(EventLine::data).collect(Collectors.toList()));
    }

    @Override
    public void addValidSession(List<EventLine> validSession) {
        valid.addAll(validSession.stream().map(EventLine::data).collect(Collectors.toList()));
    }
}
