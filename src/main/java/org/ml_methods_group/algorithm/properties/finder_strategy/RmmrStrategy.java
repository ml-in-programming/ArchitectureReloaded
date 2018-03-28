package org.ml_methods_group.algorithm.properties.finder_strategy;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

public class RmmrStrategy implements FinderStrategy {
    private static RmmrStrategy INSTANCE = new RmmrStrategy();

    @NotNull
    public static RmmrStrategy getInstance() {
        return INSTANCE;
    }

    private RmmrStrategy() {
    }

    @Override
    public boolean acceptClass(@NotNull PsiClass aClass) {
        // TODO: Accept interfaces or not?
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface());
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method) {
        // TODO: accept in interfaces?
        if (method.isConstructor() || MethodUtils.isAbstract(method)) {
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
}
