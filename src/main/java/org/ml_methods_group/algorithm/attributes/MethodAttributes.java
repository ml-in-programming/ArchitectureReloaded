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

package org.ml_methods_group.algorithm.attributes;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.RelevantProperties;

public class MethodAttributes extends ElementAttributes {
    private final @NotNull PsiMethod psiMethod;

    public MethodAttributes(
        final @NotNull PsiMethod psiMethod,
        final @NotNull double[] features,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(features, relevantProperties);
        this.psiMethod = psiMethod;
    }

    @Override
    public @NotNull PsiElement getOriginalElement() {
        return psiMethod;
    }

    public @NotNull PsiMethod getOriginalMethod() {
        return psiMethod;
    }
}
