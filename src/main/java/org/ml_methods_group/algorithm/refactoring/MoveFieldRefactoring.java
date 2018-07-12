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

package org.ml_methods_group.algorithm.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a refactoring which moves field to a target class.
 */
class MoveFieldRefactoring extends MoveToClassRefactoring {
    private final @NotNull PsiField field;

    private final @NotNull PsiClass targetClass;

    /**
     * Creates refactoring.
     *
     * @param field a field that is moved in this refactoring.
     * @param targetClass destination class in which given field is placed in this refactoring.
     * @param accuracy
     */
    public MoveFieldRefactoring(
        final @NotNull PsiField field,
        final @NotNull PsiClass targetClass,
        final double accuracy
    ) {
        super(field, targetClass, accuracy);

        this.field = field;
        this.targetClass = targetClass;
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return true;
    }

    /**
     * Returns field that is moved in this refactoring.
     */
    public @NotNull PsiField getField() {
        return field;
    }

    @Override
    public @Nullable PsiClass getContainingClass() {
        return field.getContainingClass();
    }

    @Override
    public @NotNull PsiClass getTargetClass() {
        return targetClass;
    }

    @NotNull
    public <R> R accept(final @NotNull RefactoringVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
