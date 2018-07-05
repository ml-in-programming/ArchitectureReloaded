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

package org.ml_methods_group.algorithm.properties.finder_strategy;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

/**
 * An interface for a strategy that {@link org.ml_methods_group.algorithm.entity.EntitySearcher}
 * uses. The strategy defines what kind of code entities should be skipped and what weights should
 * be assigned to them within {@link org.ml_methods_group.algorithm.entity.RelevantProperties}.
 */
public interface FinderStrategy {
    int DEFAULT_WEIGHT = 1;

    /**
     * Verifies that a file needs to be processed by the finder.
     *
     * @param file {@link PsiFile} to check.
     * @return {@code True} if file needs to be processed.
     */
    default boolean acceptFile(final PsiFile file) {
        return file != null && file.getFileType().equals(JavaFileType.INSTANCE);
    }

    /**
     * Verifies that a class needs to be processed by the finder.
     *
     * @param aClass {@link PsiClass} to check.
     * @return {@code True} if class needs to be processed.
     */
    boolean acceptClass(@NotNull final PsiClass aClass);

    /**
     * Verifies that a method needs to be processed by the finder.
     *
     * @param method {@link PsiMethod} to check.
     * @return {@code True} if method needs to be processed.
     */
    boolean acceptMethod(@NotNull final PsiMethod method);

    /**
     * Verifies that a field needs to be processed by the finder.
     *
     * @param field {@link PsiField} to check.
     * @return {@code True} if field needs to be processed.
     */
    boolean acceptField(@NotNull final PsiField field);

    boolean isRelation(@NotNull final PsiElement element);

    /** Returns {@code True} if finder should process super classes. */
    boolean processSupers();

    int getWeight(PsiMethod from, PsiClass to);

    int getWeight(PsiMethod from, PsiField to);

    int getWeight(PsiMethod from, PsiMethod to);

    int getWeight(PsiClass from, PsiField to);

    int getWeight(PsiClass from, PsiMethod to);

    int getWeight(PsiClass from, PsiClass to);

    int getWeight(PsiField from, PsiField to);

    int getWeight(PsiField from, PsiMethod to);

    int getWeight(PsiField from, PsiClass to);

    default int getWeight(Object from, Object to) {
        return DEFAULT_WEIGHT;
    }
}
