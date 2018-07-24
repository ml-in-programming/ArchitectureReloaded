package org.jetbrains.research.groups.ml_methods.generation.constraints;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.*;
import com.sixrr.metrics.utils.ClassUtils;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AcceptRelevantPropertiesGenerationConstraint implements GenerationConstraint {
    private final Map<PsiMethod, Set<PsiClass>> classesUsedInMethodBody = new HashMap<>();
    private final Map<PsiMethod, Set<PsiClass>> classesWhereMethodUsed = new HashMap<>();

    @Override
    public boolean acceptTargetClass(@NotNull PsiClass aClass) {
        return !(ClassUtils.isAnonymous(aClass) || aClass.getQualifiedName() == null
                || aClass.isEnum() || aClass.isInterface() || aClass.getQualifiedName().endsWith("Test"));
    }

    private boolean isValidMethodToMove(@NotNull PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        return  containingClass != null &&
                containingClass.getMethods().length > 1 &&
                containingClass.getQualifiedName() != null &&
                !containingClass.getQualifiedName().endsWith("Test") &&
                !method.isConstructor() &&
                !MethodUtils.isAbstract(method) &&
                !MethodUtils.isOverriding(method) &&
                !MethodUtils.isSynchronized(method) &&
                MethodUtils.isPublic(method) &&
                !MethodUtils.isStatic(method) &&
                !containingClass.isInterface() &&
                classesUsedInMethodBody.get(method).size() >= 2;
    }

    @Override
    public boolean acceptMethod(@NotNull PsiMethod method, @NotNull AnalysisScope scope) {
        classesUsedInMethodBody.put(method, new HashSet<>());
        classesWhereMethodUsed.put(method, new HashSet<>());
        method.accept(new PropertiesCalculator(method, scope));
        return isValidMethodToMove(method);
    }

    @Override
    public boolean acceptRefactoring(PsiMethod method, PsiClass aClass) {
        Set<PsiClass> usedClassesInBody = classesUsedInMethodBody.get(method);
        Set<PsiClass> classesThatCallMethod = classesWhereMethodUsed.get(method);
        return !Objects.requireNonNull(method.getContainingClass()).equals(aClass) &&
                (usedClassesInBody.contains(aClass) || classesThatCallMethod.contains(aClass));
    }

    private class PropertiesCalculator extends JavaRecursiveElementVisitor {
        private final PsiMethod method;
        private final AnalysisScope scope;

        private PropertiesCalculator(@NotNull PsiMethod method, @NotNull AnalysisScope scope) {
            this.method = method;
            this.scope = scope;
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            PsiElement element = expression.resolve();
            if (element instanceof PsiField && isClassInScope(((PsiField) element).getContainingClass())) {
                final PsiField field = (PsiField) element;
                classesUsedInMethodBody.get(method).add(field.getContainingClass());
            }
            super.visitReferenceExpression(expression);
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            final PsiMethod called = expression.resolveMethod();
            final PsiClass usedClass = called != null ? called.getContainingClass() : null;
            if (isClassInScope(usedClass)) {
                classesWhereMethodUsed.computeIfAbsent(called, ignored -> new HashSet<>()).add(method.getContainingClass());
                classesUsedInMethodBody.get(method).add(usedClass);
            }
            super.visitMethodCallExpression(expression);
        }

        @Contract("null -> false")
        private boolean isClassInScope(final @Nullable PsiClass aClass) {
            return aClass != null && scope.contains(aClass.getScope());
        }
    }
}
