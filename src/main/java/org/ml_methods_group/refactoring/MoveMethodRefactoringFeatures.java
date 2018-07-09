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

package org.ml_methods_group.refactoring;

import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.MoveMethodRefactoring;

/**
 * Subclass of {@link RefactoringFeatures} that contains features of a
 * {@link MoveMethodRefactoring}.
 */
public class MoveMethodRefactoringFeatures extends RefactoringFeatures {
    /**
     * Extracts features from a given {@link MoveMethodRefactoring}.
     *
     * @param refactoring a {@link MoveMethodRefactoring} to extract features from.
     * @param metricsRun a result of metrics calculations. Some of metrics values calculated for
     *                   objects given refactoring operates on can be used to extract refactoring
     *                   features.
     */
    public MoveMethodRefactoringFeatures(
        final @NotNull MoveMethodRefactoring refactoring,
        final @NotNull MetricsRun metricsRun
    ) {
    }
}