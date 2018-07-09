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

package org.ml_methods_group.refactoring.logging;

import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.Refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Objects of this class contain information about one user interaction with the plugin.
 * Refactorings are suggested to user and he accepts some of them and rejects the other.
 * Information about what refactorings were accepted and what refactorings were rejected is
 * stored in objects of this class. Each {@link Refactoring} is represented by
 * {@link RefactoringFeatures} derived from it. All data from this class is intended to be written
 * to log for further analysis.
 */
public final class RefactoringSessionInfo {
    private final @NotNull List<RefactoringFeatures> acceptedRefactoringsFeatures;

    private final @NotNull List<RefactoringFeatures> rejectedRefactoringsFeatures;

    /**
     * Creates session info for given accepted and rejected refactorings.
     *
     * @param acceptedRefactorings refactorings that were accepted.
     * @param rejectedRefactorings refactorings that were rejected.
     * @param metricsRun a result of metrics calculations. Used to {@link RefactoringFeatures}
     *                   of a particular {@link Refactoring}.
     */
    public RefactoringSessionInfo(
        final @NotNull List<Refactoring> acceptedRefactorings,
        final @NotNull List<Refactoring> rejectedRefactorings,
        final @NotNull MetricsRun metricsRun
    ) {
        acceptedRefactoringsFeatures =
            acceptedRefactorings.stream()
                                .map((it) -> RefactoringFeatures.extractFeatures(it, metricsRun))
                                .collect(Collectors.toList());

        rejectedRefactoringsFeatures =
                rejectedRefactorings.stream()
                        .map((it) -> RefactoringFeatures.extractFeatures(it, metricsRun))
                        .collect(Collectors.toList());
    }

    /**
     * Returns features of all accepted refactorings.
     */
    @NotNull
    public List<RefactoringFeatures> getAcceptedRefactoringsFeatures() {
        return new ArrayList<>(acceptedRefactoringsFeatures);
    }

    /**
     * Returns features of all rejected refactorings.
     */
    @NotNull
    public List<RefactoringFeatures> getRejectedRefactoringsFeatures() {
        return new ArrayList<>(rejectedRefactoringsFeatures);
    }
}
