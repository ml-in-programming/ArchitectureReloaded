package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import java.util.List;

public interface SessionValidationResult {
    void addErrorSession(List<EventLine> errorSession);
    void addValidSession(List<EventLine> validSession);
}
