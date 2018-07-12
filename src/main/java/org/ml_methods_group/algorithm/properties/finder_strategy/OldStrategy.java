package org.ml_methods_group.algorithm.properties.finder_strategy;

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
