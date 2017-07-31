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

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class NewStrategy implements FinderStrategy {
    private final int PRIVATE_MEMBER_ACCESS_WEIGHT = 2 * DEFAULT_WEIGHT;

    private static NewStrategy INSTANCE = new NewStrategy();

    @NotNull
    public static NewStrategy getInstance() {
        return INSTANCE;
    }

    private NewStrategy() {
    }

    @Override
    public boolean acceptClass(@NotNull PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method) {
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
            return false;
        }
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface());
    }

    @Override
    public boolean acceptField(@NotNull PsiField field) {
        return field.getContainingClass() != null;
    }

    @Override
    public boolean isRelation(@NotNull PsiElement element) {
        final PsiElement e = PsiTreeUtil.getDeepestFirst(element).getParent();
        if (!(e instanceof PsiReferenceExpression)) {
            return false;
        }
        final PsiElement resolved = ((PsiReferenceExpression) e).resolve();
        return resolved instanceof PsiField || resolved instanceof PsiClass || resolved instanceof PsiMethod ||
                resolved instanceof PsiThisExpression;
    }

    @Override
    public boolean processSupers() {
        return false;
    }

    @Override
    public int getWeight(PsiMethod from, PsiField to) {
        if (to.hasModifierProperty(PsiModifier.PRIVATE)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiMethod to) {
        if (to.hasModifierProperty(PsiModifier.PRIVATE)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiClass to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiField to) {
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
