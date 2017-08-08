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
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class NewStrategy implements FinderStrategy {
    private final int PRIVATE_MEMBER_ACCESS_WEIGHT = 6 * DEFAULT_WEIGHT;
    private final int GETTER_ACCESS_WEIGHT = DEFAULT_WEIGHT;
    private final int PUBLIC_STATIC_FIELD_ACCESS_WEIGHT = 0;
    private final int PUBLIC_STATIC_METHOD_ACCESS_WEIGHT = 0;
    private final int MEMBER_ACCESS_WEIGHT = 4 * DEFAULT_WEIGHT;
    private final int ITSELF_WEIGHT = PRIVATE_MEMBER_ACCESS_WEIGHT;

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
                || aClass.isEnum() || aClass.isInterface());
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
        return true;
//        final PsiElement e = PsiTreeUtil.getDeepestFirst(element).getParent();
//        if (!(e instanceof PsiReferenceExpression)) {
//            return false;
//        }
//        final PsiElement resolved = ((PsiReferenceExpression) e).resolve();
//        return resolved instanceof PsiField || resolved instanceof PsiClass || resolved instanceof PsiMethod ||
//                resolved instanceof PsiThisExpression || resolved instanceof PsiSuperExpression;
    }

    @Override
    public boolean processSupers() {
        return false;
    }

    @Override
    public int getWeight(PsiMethod from, PsiField to) {
        if (!to.hasModifierProperty(PsiModifier.PUBLIC)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        if (to.hasModifierProperty(PsiModifier.STATIC)) {
            return PUBLIC_STATIC_FIELD_ACCESS_WEIGHT;
        }
        return MEMBER_ACCESS_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiMethod to) {
        if (from.equals(to)) {
            return ITSELF_WEIGHT;
        }
        if (MethodUtils.isGetter(to)) {
            return GETTER_ACCESS_WEIGHT;
        }
        if (!to.hasModifierProperty(PsiModifier.PUBLIC)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        if (to.hasModifierProperty(PsiModifier.STATIC)) {
            return PUBLIC_STATIC_METHOD_ACCESS_WEIGHT;
        }
        return MEMBER_ACCESS_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiClass to) {
        if (to.equals(from.getContainingClass())) {
            return getWeight(to, from);
        }
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiField to) {
        if (!to.hasModifierProperty(PsiModifier.PUBLIC)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        return MEMBER_ACCESS_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiMethod to) {
        if (!to.hasModifierProperty(PsiModifier.PUBLIC)) {
            return PRIVATE_MEMBER_ACCESS_WEIGHT;
        }
        return MEMBER_ACCESS_WEIGHT;
    }

    @Override
    public int getWeight(PsiClass from, PsiClass to) {
        if (from.equals(to)) {
            return ITSELF_WEIGHT;
        }
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiField from, PsiField to) {
        if (from.equals(to)) {
            return ITSELF_WEIGHT;
        }
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiField from, PsiMethod to) {
        return getWeight(to, from);
    }

    @Override
    public int getWeight(PsiField from, PsiClass to) {
        if (to.equals(from.getContainingClass())) {
            return getWeight(to, from);
        }
        return DEFAULT_WEIGHT;
    }
}
