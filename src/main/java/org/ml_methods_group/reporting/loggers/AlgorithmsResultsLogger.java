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

package org.ml_methods_group.reporting.loggers;

import com.intellij.openapi.application.ApplicationManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.AlgorithmResult;
import org.ml_methods_group.algorithm.Refactoring;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.reporting.Reporter;
import org.ml_methods_group.reporting.StatsSender;

import java.util.List;

public class AlgorithmsResultsLogger implements StatsLogger {
    private static final Logger LOG = Logging.getLogger(AlgorithmsResultsLogger.class);

    private final String recorderId = "algorithms";
    private final String actionType = "calculated";
    private final String recorderVersion = "1.0";
    private static AlgorithmsResultsLogger ourInstance = new AlgorithmsResultsLogger();

    public static AlgorithmsResultsLogger getInstance() {
        return ourInstance;
    }

    private AlgorithmsResultsLogger() {
    }

    public void dump(@NotNull RefactoringExecutionContext context) {
        dump(getHolder(context));
    }

    @Override
    public void dump(@NotNull Object data) {
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final String reportLine = Reporter.createReportLine(recorderId, recorderVersion, actionType, data);
            if (!StatsSender.send(reportLine, true)) {
                LOG.debug("Error while reporting algorithms results");
            }
        });
    }

    private static AlgorithmsResultsInfo getHolder(@NotNull RefactoringExecutionContext context) {
        return new AlgorithmsResultsInfo(context);
    }

    private static class AlgorithmsResultsInfo {
        private ResultInfo[] results;
        private ProjectInfo project;

        AlgorithmsResultsInfo(@NotNull final RefactoringExecutionContext context) {
            results = context.getAlgorithmResults().stream().map(ResultInfo::new).toArray(ResultInfo[]::new);
            project = new ProjectInfo(context);
        }

        private static class ResultInfo {
            private String algorithm;
            private int count;
            private double[] accuracies;
            private long executionTime;

            ResultInfo(AlgorithmResult result) {
                algorithm = result.getAlgorithmName();
                List<Refactoring> refactorings = result.getRefactorings();
                count = refactorings.size();
                accuracies = refactorings.stream().mapToDouble(Refactoring::getAccuracy).toArray();
                executionTime = result.getExecutionTime();
            }
        }

        private static class ProjectInfo {
            private int classCount;
            private int methodsCount;
            private int fieldsCount;

            ProjectInfo(@NotNull RefactoringExecutionContext context) {
                classCount = context.getClassCount();
                methodsCount = context.getMethodsCount();
                fieldsCount = context.getFieldsCount();
            }
        }
    }
}
