package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

public class NumOverloadsCalculator extends MethodCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null) {
                postMetric(method, MethodUtils.getNumberOfOverloads(method, containingClass, true));
            }
            super.visitMethod(method);
        }
    }
}
