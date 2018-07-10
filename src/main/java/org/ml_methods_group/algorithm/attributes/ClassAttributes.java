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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.RelevantProperties;

public class ClassAttributes extends ElementAttributes {
    private final @NotNull PsiClass psiClass;

    public ClassAttributes(
        final @NotNull PsiClass psiClass,
        final @NotNull double[] features,
        final @NotNull RelevantProperties relevantProperties
    ) {
        super(features, relevantProperties);
        this.psiClass = psiClass;
    }

    @Override
    public @NotNull PsiElement getOriginalElement() {
        return psiClass;
    }

    public @NotNull PsiClass getOriginalClass() {
        return psiClass;
    }
}
