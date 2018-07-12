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

package org.jetbrains.research.groups.ml_methods.algorithm.properties.finder_strategy;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class OldStrategy implements FinderStrategy {

    private static OldStrategy INSTANCE = new OldStrategy();

    @NotNull
    public static OldStrategy getInstance() {
        return INSTANCE;
    }

    private OldStrategy() {
    }

    @Override
    public boolean acceptClass(@NotNull PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface());
    }

    @Override
    public boolean acceptField(@NotNull PsiField field) {
        return field.getContainingClass() != null;
    }

    @Override
    public boolean isRelation(@NotNull PsiElement element) {
        return true;
    }

    @Override
    public boolean processSupers() {
        return true;
    }

    @Override
    public int getWeight(PsiMethod from, PsiField to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiField to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiClass to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiMethod to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiMethod to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiClass to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiField from, PsiField to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiField from, PsiMethod to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiField from, PsiClass to) {
        return DEFAULT_WEIGHT;
    }
}
