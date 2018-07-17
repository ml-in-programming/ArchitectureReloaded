package org.jetbrains.research.groups.ml_methods.extraction.info;

import com.intellij.psi.*;
import com.sixrr.metrics.utils.MethodUtils;
import com.sun.xml.internal.xsom.impl.scd.Iterators;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

    public @NotNull Stream<PsiMethod> getSameInstanceCallers(
        final @NotNull Predicate<? super PsiMethod>... filters
    ) {
        return createFilteredStream(sameInstanceCallers, filters);
    }

    /**
     * Returns {@link List} of methods that call method this info object is created for not through
     * {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherInstanceCallers() {
        return Collections.unmodifiableList(anotherInstanceCallers);
    }

    public @NotNull Stream<PsiMethod> getAnotherInstanceCallers(
        final @NotNull Predicate<? super PsiMethod>... filters
    ) {
        return createFilteredStream(anotherInstanceCallers, filters);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * through {@code this} object.
     */
    public @NotNull List<PsiMethod> getSameInstanceTargets() {
        return Collections.unmodifiableList(sameInstanceTargets);
    }

    public @NotNull Stream<PsiMethod> getSameInstanceTargets(
        final @NotNull Predicate<? super PsiMethod>... filters
    ) {
        return createFilteredStream(sameInstanceTargets, filters);
    }

    /**
     * Returns {@link List} of methods that are called by method this info object is created for
     * not through {@code this} object.
     */
    public @NotNull List<PsiMethod> getAnotherInstanceTargets() {
        return Collections.unmodifiableList(anotherInstanceTargets);
    }

    public @NotNull Stream<PsiMethod> getAnotherInstanceTargets(
        final @NotNull Predicate<? super PsiMethod>... filters
    ) {
        return createFilteredStream(anotherInstanceTargets, filters);
    }

    /**
     * Returns {@link List} of all fields that are accessed by method this info object is created
     * for.
     */
    public @NotNull List<PsiField> getAccessedFields() {
        return Collections.unmodifiableList(accessedFields);
    }

    public @NotNull Stream<PsiField> getAccessedFields(
        final @NotNull Predicate<? super PsiField>... filters
    ) {
        return createFilteredStream(accessedFields, filters);
    }

    private static <T> Stream<T> createFilteredStream(
        final @NotNull List<T> list,
        final @NotNull Predicate<? super T>[] filters
    ) {
        return list.stream()
                .filter(it -> Arrays.stream(filters).allMatch(itt -> itt.test(it)));
    }

    public static class Builder {
        private final @NotNull PsiMethod method;

        private final @NotNull Set<PsiMethod> sameInstanceCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherObjectCallers = new HashSet<>();

        private final @NotNull Set<PsiMethod> sameObjectTargets = new HashSet<>();

        private final @NotNull Set<PsiMethod> anotherObjectTargets = new HashSet<>();

        private final @NotNull Set<PsiField> accessedFields = new HashSet<>();

        public Builder(final @NotNull PsiMethod method) {
            this.method = method;
        }

        public @NotNull MethodInfo build() {
            return new MethodInfo(
                new ArrayList<>(sameInstanceCallers),
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
                sameInstanceCallers.add(caller);
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
