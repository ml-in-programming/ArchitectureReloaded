/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.research.groups.ml_methods.config;

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
