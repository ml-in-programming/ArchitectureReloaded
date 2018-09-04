package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

public class NumMeaningfulClassesMethodCalculator extends MethodCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);

            postMetric(method, MethodUtils.getMeaningfulClasses(method).size());
        }
    }
}
