package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.openapi.util.Key;
import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.sixrr.stockmetrics.utils.MethodCallMap;
import com.sixrr.stockmetrics.utils.MethodCallMapImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.utils.PSIUtil;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractNumMethodsThatCallCalculator extends MethodCalculator {
    private final @NotNull MethodsFilter methodsFilter;

    public AbstractNumMethodsThatCallCalculator(final @NotNull MethodsFilter methodsFilter) {
        this.methodsFilter = methodsFilter;
    }

    @Override
    protected PsiElementVisitor createVisitor() {
        return new Visitor();
    }

    public interface MethodsFilter {
        boolean filter(
            @NotNull PsiMethod callingMethod,
            @NotNull PsiMethod currentMethod
        );
    }

    private class Visitor extends JavaRecursiveElementVisitor {
        @Override
        public void visitMethod(PsiMethod method) {
            super.visitMethod(method);

            final Key<MethodCallMap> key = new Key<MethodCallMap>("MethodCallMap");

            MethodCallMap methodCallMap = executionContext.getUserData(key);
            if (methodCallMap == null) {
                methodCallMap = new MethodCallMapImpl();
                executionContext.putUserData(key, methodCallMap);
            }

            final Set<PsiReference> methodCalls = methodCallMap.calculateMethodCallPoints(method);

            long calls =
                methodCalls.stream()
                    .map(PsiReference::getElement)
                    .map(PSIUtil::getParentMethod)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .distinct()
                    .filter(it -> methodsFilter.filter(it, method)).count();

            postMetric(method, calls);
        }
    }
}
