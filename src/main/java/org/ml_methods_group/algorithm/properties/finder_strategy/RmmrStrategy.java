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

package org.ml_methods_group.algorithm.properties.finder_strategy;

import com.intellij.psi.*;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Describes strategy for searching entities.
 * For example: we do not accept enum classes because method couldn't (very rarely) be moved there.
 * This implementation is singleton object.
 */
public class RmmrStrategy implements FinderStrategy {
    private static RmmrStrategy INSTANCE = new RmmrStrategy();
    private boolean acceptPrivateMethods;
    private boolean acceptMethodParams;
    /**
     * Check for expressions like this: instanceOfClassNotInScope.publicFieldWithNoInformationForContext.
     * This situation may occur when field is public or protected or package private but we do not consider its class
     * (because it can be external class in jar or something like that).
     */
    private boolean checkPsiVariableForBeingInScope;
    private boolean acceptNewExpressions;
    private boolean acceptInnerClasses;
    private boolean applyStemming;
    private int minimalTermLength;

    /**
     * Get instance of singleton object.
     *
     * @return instance of this class.
     */
    @NotNull
    public static RmmrStrategy getInstance() {
        return INSTANCE;
    }

    private RmmrStrategy() {
    }

    @Override
    public boolean acceptClass(@NotNull PsiClass aClass) {
        // TODO: Accept interfaces or not?
        // TODO: Accept inner and nested classes or not?
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method) {
        // TODO: accept in interfaces?
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
            return false;
        }
        if (!acceptPrivateMethods && method.getModifierList().hasModifierProperty(PsiModifier.PRIVATE)) {
            return false;
        }
        final PsiClass containingClass = method.getContainingClass();
        return !(containingClass == null || containingClass.isInterface() || !acceptClass(containingClass));
    }

    @Override
    public boolean acceptField(@NotNull PsiField field) {
        return false;
    }

    @Override
    public boolean isRelation(@NotNull PsiElement element) {
        return true;
    }

    @Override
    public boolean processSupers() {
        return false;
    }

    @Override
    public int getWeight(PsiMethod from, PsiClass to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiField to) {
        return DEFAULT_WEIGHT;
    }

    @Override
    public int getWeight(PsiMethod from, PsiMethod to) {
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
        return 0;
    }

    @Override
    public int getWeight(PsiField from, PsiMethod to) {
        return 0;
    }

    @Override
    public int getWeight(PsiField from, PsiClass to) {
        return 0;
    }

    public void setAcceptPrivateMethods(boolean acceptPrivateMethods) {
        this.acceptPrivateMethods = acceptPrivateMethods;
    }

    public void setCheckPsiVariableForBeingInScope(boolean checkPsiVariableForBeingInScope) {
        this.checkPsiVariableForBeingInScope = checkPsiVariableForBeingInScope;
    }

    public boolean getCheckPsiVariableForBeingInScope() {
        return checkPsiVariableForBeingInScope;
    }

    public boolean isAcceptMethodParams() {
        return acceptMethodParams;
    }

    public void setAcceptMethodParams(boolean acceptMethodParams) {
        this.acceptMethodParams = acceptMethodParams;
    }

    public boolean isAcceptNewExpressions() {
        return acceptNewExpressions;
    }

    public void setAcceptNewExpressions(boolean acceptNewExpressions) {
        this.acceptNewExpressions = acceptNewExpressions;
    }

    public void setAcceptInnerClasses(boolean acceptInnerClasses) {
        this.acceptInnerClasses = acceptInnerClasses;
    }

    public boolean isAcceptInnerClasses() {
        return acceptInnerClasses;
    }

    public void setApplyStemming(boolean applyStemming) {
        this.applyStemming = applyStemming;
    }

    public boolean isApplyStemming() {
        return applyStemming;
    }

    public int getMinimalTermLength() {
        return minimalTermLength;
    }

    public void setMinimalTermLength(int minimalTermLength) {
        this.minimalTermLength = minimalTermLength;
    }
}
