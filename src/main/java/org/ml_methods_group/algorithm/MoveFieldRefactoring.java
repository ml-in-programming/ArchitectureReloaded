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

package org.ml_methods_group.algorithm;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.utils.PsiSearchUtil;

/**
 * TODO: getters, hashcode, equals, toString
 */
class MoveFieldRefactoring extends Refactoring {
    private final @NotNull PsiField field;

    private final @NotNull PsiClass targetClass;

    public MoveFieldRefactoring(
        final @NotNull PsiField field,
        final @NotNull PsiClass targetClass,
        final double accuracy
    ) {
        super(
            PsiSearchUtil.getHumanReadableName(field),
            PsiSearchUtil.getHumanReadableName(targetClass),
            accuracy
        );

        this.field = field;
        this.targetClass = targetClass;
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return true;
    }
}