/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.config;

import org.apache.log4j.*;

import java.io.IOException;

public class Logging {
    public static Logger getLogger(Class<?> aClass) {
        final Logger logger = Logger.getLogger(aClass);
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
        return logger;
    }

    public static Logger getRefactoringLogger(Class<?> aClass) throws IOException {
        final Logger logger = Logger.getLogger(aClass.getName() + "-refactoring");
        logger.setLevel(Level.INFO);
        logger.addAppender(new FileAppender(new PatternLayout("%p [%c.%M] - %m%n"), "~/ArchitectureReloaded/log/refactorings.log", true));
        return logger;
    }
}
