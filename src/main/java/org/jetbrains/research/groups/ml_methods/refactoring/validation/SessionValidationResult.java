package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import java.util.List;

/** An interface for a server-side code to obtain correct and incorrect log entries from a validator */
public interface SessionValidationResult {
    void addErrorSession(List<EventLine> errorSession);
    void addValidSession(List<EventLine> validSession);
}
