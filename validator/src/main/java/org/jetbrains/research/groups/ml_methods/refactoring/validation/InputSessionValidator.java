package org.jetbrains.research.groups.ml_methods.refactoring.validation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

/** Server-side validator for incoming log entries */
public class InputSessionValidator {
    private org.jetbrains.research.groups.ml_methods.refactoring.validation.SessionValidationResult result;

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

    private final String OUR_RECORDER_ID = "architecture-reloaded-plugin";
    private final String UNCHECKED_TAG = "uncheckedRefactoringsFeatures";
    private final String REJECTED_TAG = "rejectedRefactoringsFeatures";
    private final String APPLIED_TAG = "appliedRefactoringsFeatures";


    /** Takes empty @link{SessionValidationResult} which will be populated in @link{#validate} */
    public InputSessionValidator(org.jetbrains.research.groups.ml_methods.refactoring.validation.SessionValidationResult result) {
        this.result = result;
    }

    /**
     *  Iterates through the log entries and checks is they are correct or not.
     *  The result is put into the @link{#result} field
     * @param input a collection of incoming log entries
     */
    public void validate(Iterable<String> input) {
        List<EventLine> errorSession = new ArrayList<>();
        List<EventLine> validSession = new ArrayList<>();

        for (String line : input) {
            if (line.trim().isEmpty())
                continue;

            String[] fields = line.split("\t", -1);
            if (!isFormattedCorrectly(fields)) {
                errorSession.add(new EventLine(line));
                continue;
            }

            String payload = fields[7];
            if (isCorrectPayload(payload)) {
                validSession.add(new EventLine(payload));
            } else {
                errorSession.add(new EventLine(payload));
            }
        }

        result.addErrorSession(errorSession);
        result.addValidSession(validSession);
    }

    private boolean isFormattedCorrectly(String[] fields) {
        if (fields.length != 8)
            return false;

        try {
            long timestamp = Long.parseLong(fields[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        String uuidRegex = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

        boolean userIdCorrect = fields[3].matches(uuidRegex);
        boolean sessionIdCorrect = fields[4].matches(uuidRegex);

        return fields[1].equals(OUR_RECORDER_ID)
                && userIdCorrect
                && sessionIdCorrect;
    }

    private boolean isCorrectPayload(String payload) {
        if (payload.isEmpty())
            return false;

        try {
            Gson gson = new Gson();
            LinkedTreeMap json = gson.fromJson(payload, LinkedTreeMap.class);
            if (json.isEmpty()
                    || !json.containsKey(UNCHECKED_TAG)
                    || !json.containsKey(REJECTED_TAG)
                    || !json.containsKey(APPLIED_TAG)
                    || ( ((ArrayList) json.get(UNCHECKED_TAG)).isEmpty() &&
                         ((ArrayList) json.get(REJECTED_TAG)).isEmpty() &&
                         ((ArrayList) json.get(APPLIED_TAG)).isEmpty()
                       )
            ) {
                return false;
            }
        } catch (JsonSyntaxException ignored) {
            return false;
        }

        return true;
    }

    public static void main(String args[]) {
        System.out.println("working fine.");
    }
}
