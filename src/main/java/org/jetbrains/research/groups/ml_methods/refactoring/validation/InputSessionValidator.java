package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class InputSessionValidator {
    private SessionValidationResult result;

    private enum LogEntryFields {
        TIMESTAMP,
        RECORDER_ID,
        RECORDER_VERSION,
        USER_UID,
        SESSION_UID,
        BUCKET,
        ACTION_TYPE,
        PAYLOAD
    }

    public InputSessionValidator(SessionValidationResult result) {
        this.result = result;
    }

    public void validate(Iterable<String> input) {
        List<EventLine> errorSession = new ArrayList<>();
        List<EventLine> validSession = new ArrayList<>();

        for (String line : input) {
            if (line.trim().isEmpty())
                continue;

            String[] fields = line.split("\t", -1);
            if (fields.length != 8 || !fields[1].equals("architecture-reloaded-plugin")) {
                errorSession.add(new EventLine(line));
                continue;
            }

            String payload = fields[7];

            try {
                Gson gson = new Gson();
                gson.fromJson(payload, Object.class);
                validSession.add(new EventLine(payload));
            } catch(JsonSyntaxException ex) {
                errorSession.add(new EventLine(payload));
            }
        }

        result.addErrorSession(errorSession);
        result.addValidSession(validSession);
    }
}
