package org.jetbrains.research.groups.ml_methods.refactoring.validation;

/**
 * Represents internal structure of a log entry. Since we don't really need it so far,
 * now it's just a simple wrapper for the raw log data
 */
public class EventLine {
    private final String data;

    public EventLine(String data) {
        this.data = data;
    }

    public String data() {
        return data;
    }
}
