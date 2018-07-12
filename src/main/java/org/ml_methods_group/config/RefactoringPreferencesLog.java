package org.ml_methods_group.config;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.helpers.LogLog;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Class which contains log for refactoring preferences. Log is local and is stored on user's
 * machine. Every time user chooses some refactorings and declines the rest of them a new entry
 * must be written into this log. Later this information can be used to infer user's preferences
 * and make better suggestions for that particular user.
 */
public class RefactoringPreferencesLog {
    private RefactoringPreferencesLog() {}

    public static final @NotNull Logger log = Logger.getLogger(RefactoringPreferencesLog.class);

    static {
        String logFileName = Paths.get(
            System.getProperty("user.home"),
            "ArchitectureReloaded",
            "log",
            "preferences-info.log"
        ).toString();

        try {
            log.addAppender(new FileAppender(new PatternLayout("%m%n"), logFileName));
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + logFileName);
        }
    }
}
