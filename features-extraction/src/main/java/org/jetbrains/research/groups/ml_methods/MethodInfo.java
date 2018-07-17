package org.jetbrains.research.groups.ml_methods;

import com.intellij.psi.*;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class contains structural information gathered for a particular method. It stores methods
 * that call this method, methods that this method calls etc.
 */
public class MethodInfo {
    private final @NotNull List<PsiMethod> sameObjectCallers;

    private final @NotNull List<PsiMethod> anotherObjectCallers;

    private final @NotNull List<PsiMethod> sameObjectTargets;

    private final @NotNull List<PsiMethod> anotherObjectTargets;

    private final @NotNull List<PsiField> accessedFields;

    private MethodInfo(
        final @NotNull List<PsiMethod> sameObjectCallers,
        final @NotNull List<PsiMethod> anotherObjectCallers,
        final @NotNull List<PsiMethod> sameObjectTargets,
        final @NotNull List<PsiMethod> anotherObjectTargets,
        final @NotNull List<PsiField> accessedFields
    ) {
        this.sameObjectCallers = sameObjectCallers;
        this.anotherObjectCallers = anotherObjectCallers;
        this.sameObjectTargets = sameObjectTargets;
        this.anotherObjectTargets = anotherObjectTargets;
        this.accessedFields = accessedFields;
    }

    /**
     * Returns {@link List} of methods that call method this info object is created for through
     * {@code this} object.
     */
    public @NotNull List<PsiMethod> getSameObjectCallers() {
        return Collections.unmodifiableList(sameObjectCallers);
    }

    /**
     * Returns {@link List} of methods that call method this info object is created for not through
     * {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherObjectCallers() {
        return Collections.unmodifiableList(anotherObjectCallers);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * through {@code this} object.
     */
    public @NotNull List<PsiMethod> getSameObjectTargets() {
        return Collections.unmodifiableList(sameObjectTargets);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * not through {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherObjectTargets() {
        return Collections.unmodifiableList(anotherObjectTargets);
    }

    /**
     * Returns {@link List} of all fields that are accessed by method this info object is created
     * for.
     */
    public @NotNull List<PsiField> getAccessedFields() {
        return Collections.unmodifiableList(accessedFields);
    }

    public static class Builder {
        private final @NotNull PsiMethod method;

        private final @NotNull Set<PsiMethod> sameObjectCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherObjectCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> sameObjectTargets = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherObjectTargets = new HashSet<>();

        private final @NotNull Set<PsiField> accessedFields = new HashSet<>();

        public Builder(final @NotNull PsiMethod method) {
            this.method = method;
        }

        public @NotNull MethodInfo build() {
            return new MethodInfo(
                new ArrayList<>(sameObjectCallers),
                new ArrayList<>(anotherObjectCallers),
                new ArrayList<>(sameObjectTargets),
                new ArrayList<>(anotherObjectTargets),
                new ArrayList<>(accessedFields)
            );
        }

        public void addCaller(
            final @NotNull PsiMethod caller,
            final @NotNull PsiMethodCallExpression expression
        ) {
            if (isCallFromTheSameObject(method, expression)) {
                sameObjectCallers.add(caller);
            } else {
                anotherObjectCallers.add(caller);
            }
        }

        public void addTarget(
            final @NotNull PsiMethod target,
            final @NotNull PsiMethodCallExpression expression
        ) {
            if (isCallFromTheSameObject(target, expression)) {
                sameObjectTargets.add(target);
            } else {
                anotherObjectTargets.add(target);
            }
        }

        public void addFieldAccess(final @NotNull PsiField field) {
            accessedFields.add(field);
        }

        private boolean isCallFromTheSameObject(
            final @NotNull PsiMethod target,
            final @NotNull PsiMethodCallExpression expression
        ) {
            PsiReferenceExpression referenceExpression = expression.getMethodExpression();
            if (referenceExpression.isQualified()) {
                return referenceExpression.getQualifierExpression() instanceof PsiThisExpression;
            } else {
                return !MethodUtils.isStatic(target);
            }
        }
    }
}
