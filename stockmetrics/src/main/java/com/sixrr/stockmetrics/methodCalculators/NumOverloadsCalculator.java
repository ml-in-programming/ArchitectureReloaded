package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;

public class NumOverloadsCalculator extends MethodCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            int numberOfOverloads = 0;
            PsiClass containingClass = method.getContainingClass();
            if (containingClass != null) {
                for (PsiMethod methodInClass : containingClass.getMethods()) {
                    if (methodInClass.getName().equals(method.getName())) {
                        numberOfOverloads++;
                    }
                }
                numberOfOverloads--;
            }
            postMetric(method, numberOfOverloads);
            super.visitMethod(method);
        }
    }
}
