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

package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Refactoring;

/**
 * This class contains features extracted from some {@link Refactoring}. This features should
 * characterize {@link Refactoring} in a way that helps to extract user refactorings preferences
 * from features of accepted and rejected refactorings.
 */
public class RefactoringFeatures {
    private final boolean isFieldMove;

    /**
     * Extracts features from a given {@link Refactoring}.
     *
     * @param refactoring a {@link Refactoring} to extract features from.
     */
    public RefactoringFeatures(final @NotNull Refactoring refactoring) {
        isFieldMove = refactoring.isUnitField();
    }

    public boolean isFieldMove() {
        return isFieldMove;
    }
}
