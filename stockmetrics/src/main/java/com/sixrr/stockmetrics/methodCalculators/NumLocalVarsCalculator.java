package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

public class NumLocalVarsCalculator extends MethodCalculator {
    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        private int methodNestingDepth = 0;

        private int elementCount = 0;

        @Override
        public void visitMethod(PsiMethod method) {
            if (methodNestingDepth == 0) {
                elementCount = 0;
            }

            methodNestingDepth++;
            super.visitMethod(method);
            methodNestingDepth--;

            if (methodNestingDepth == 0 && !MethodUtils.isAbstract(method)) {
                postMetric(method, elementCount);
            }
        }

        @Override
        public void visitLocalVariable(PsiLocalVariable variable) {
            super.visitLocalVariable(variable);
            elementCount++;
        }
    }
}
