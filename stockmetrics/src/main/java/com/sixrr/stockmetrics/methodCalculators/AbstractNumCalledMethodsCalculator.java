package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNumCalledMethodsCalculator extends MethodCalculator {
    private boolean isInsideMethod = false;

    private Set<PsiMethod> calledMethods = new HashSet<>();

    private PsiMethod currentMethod = null;

    private final @NotNull MethodsFilter methodsFilter;

    public AbstractNumCalledMethodsCalculator(final @NotNull MethodsFilter methodsFilter) {
        this.methodsFilter = methodsFilter;
    }

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    public interface MethodsFilter {
        boolean filter(
            @NotNull PsiMethod calledMethod,
            @NotNull PsiMethod currentMethod
        );
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            if (!isInsideMethod) {
                calledMethods.clear();
                currentMethod = method;
            } else {
                return;
            }

            isInsideMethod = true;
            super.visitMethod(method);
            isInsideMethod = false;

            postMetric(method, calledMethods.size());
        }

        @Override
        public void visitClass(PsiClass aClass) {
            if (isInsideMethod) {
                return;
            }

            super.visitClass(aClass);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);

            PsiMethod calledMethod = expression.resolveMethod();
            if (calledMethod == null) {
                return;
            }

            if (methodsFilter.filter(calledMethod, currentMethod)) {
                calledMethod.add(calledMethod);
            }
        }
    }
}
