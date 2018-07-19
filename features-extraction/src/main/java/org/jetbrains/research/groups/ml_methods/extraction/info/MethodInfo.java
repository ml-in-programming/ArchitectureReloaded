package org.jetbrains.research.groups.ml_methods.extraction.info;

import com.intellij.psi.*;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This class contains structural information gathered for a particular method. It stores methods
 * that call this method, methods that this method calls etc.
 */
public class MethodInfo {
    private final @NotNull List<PsiMethod> sameInstanceCallers;

    private final @NotNull List<PsiMethod> anotherInstanceCallers;

    private final @NotNull List<PsiMethod> sameInstanceTargets;

    private final @NotNull List<PsiMethod> anotherInstanceTargets;

    private final @NotNull List<PsiField> accessedFields;

    private MethodInfo(
        final @NotNull List<PsiMethod> sameInstanceCallers,
        final @NotNull List<PsiMethod> anotherInstanceCallers,
        final @NotNull List<PsiMethod> sameInstanceTargets,
        final @NotNull List<PsiMethod> anotherInstanceTargets,
        final @NotNull List<PsiField> accessedFields
    ) {
        this.sameInstanceCallers = sameInstanceCallers;
        this.anotherInstanceCallers = anotherInstanceCallers;
        this.sameInstanceTargets = sameInstanceTargets;
        this.anotherInstanceTargets = anotherInstanceTargets;
        this.accessedFields = accessedFields;
    }

    /**
     * Returns {@link List} of methods that call method this info object is created for through
     * {@code this} object.
     */
    public @NotNull List<PsiMethod> getSameInstanceCallers() {
        return Collections.unmodifiableList(sameInstanceCallers);
    }

    /**
     * Returns {@link List} of methods that call method this info object is created for not through
     * {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherInstanceCallers() {
        return Collections.unmodifiableList(anotherInstanceCallers);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * through {@code this} object.
     */
    public @NotNull List<PsiMethod> getSameInstanceTargets() {
        return Collections.unmodifiableList(sameInstanceTargets);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * not through {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherInstanceTargets() {
        return Collections.unmodifiableList(anotherInstanceTargets);
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

        private final @NotNull Set<PsiMethod> sameInstanceCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherInstanceCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> sameInstanceTargets = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherInstanceTargets = new HashSet<>();

        private final @NotNull Set<PsiField> accessedFields = new HashSet<>();

        public Builder(final @NotNull PsiMethod method) {
            this.method = method;
        }

        public @NotNull MethodInfo build() {
            return new MethodInfo(
                new ArrayList<>(sameInstanceCallers),
                new ArrayList<>(anotherInstanceCallers),
                new ArrayList<>(sameInstanceTargets),
                new ArrayList<>(anotherInstanceTargets),
                new ArrayList<>(accessedFields)
            );
        }

        public void addCaller(
            final @NotNull PsiMethod caller,
            final @NotNull PsiMethodCallExpression expression
        ) {
            if (isCallFromTheSameObject(method, expression)) {
                sameInstanceCallers.add(caller);
            } else {
                anotherInstanceCallers.add(caller);
            }
        }

        public void addTarget(
            final @NotNull PsiMethod target,
            final @NotNull PsiMethodCallExpression expression
        ) {
            if (isCallFromTheSameObject(target, expression)) {
                sameInstanceTargets.add(target);
            } else {
                anotherInstanceTargets.add(target);
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
