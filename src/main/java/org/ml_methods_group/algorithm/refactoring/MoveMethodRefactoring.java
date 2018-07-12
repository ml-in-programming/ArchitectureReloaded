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
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Representation of a refactoring which moves method to a target class.
 */
public class MoveMethodRefactoring extends MoveToClassRefactoring {
    private final @NotNull PsiMethod method;

    private final @NotNull PsiClass targetClass;

    /**
     * Creates refactoring.
     *
     * @param method a method that is moved in this refactoring.
     * @param targetClass destination class in which given method is placed in this refactoring.
     * @param accuracy
     */
    public MoveMethodRefactoring(
        final @NotNull PsiMethod method,
        final @NotNull PsiClass targetClass,
        final double accuracy
    ) {
        super(method, targetClass, accuracy);

        this.method = method;
        this.targetClass = targetClass;
    }

    @Override
    public boolean isMoveFieldRefactoring() {
        return false;
    }

    /**
     * Returns method that is moved in this refactoring.
     */
    public @NotNull PsiMethod getMethod() {
        return method;
    }

    @Override
    public @Nullable PsiClass getContainingClass() {
        return method.getContainingClass();
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
