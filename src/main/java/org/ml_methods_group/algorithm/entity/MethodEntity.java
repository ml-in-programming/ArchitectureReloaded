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

package org.ml_methods_group.algorithm.entity;

import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class MethodEntity extends CodeEntity {
    private final @NotNull
    PsiMethod psiMethod;

    public MethodEntity(
        final @NotNull PsiMethod psiMethod,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(relevantProperties);
        this.psiMethod = psiMethod;
    }

    @Override
    public @NotNull String getIdentifier() {
        return MethodUtils.calculateSignature(psiMethod);
    }

    @Override
    public @NotNull
    MetricCategory getMetricCategory() {
        return MetricCategory.Method;
    }

    public @NotNull PsiMethod getPsiMethod() {
        return psiMethod;
    }
}
