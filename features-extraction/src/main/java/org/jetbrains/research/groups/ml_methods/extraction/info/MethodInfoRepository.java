package org.jetbrains.research.groups.ml_methods.extraction.info;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This is a repository of {@link MethodInfo}. It stores {@link MethodInfo} objects for each method
 * that has been encountered during analysis.
 */
public class MethodInfoRepository {
    private final @NotNull Map<PsiMethod, MethodInfo> methods;

    private MethodInfoRepository(final @NotNull Map<PsiMethod, MethodInfo> methods) {
        this.methods = methods;
    }

    /**
     * Returns all the methods that have been put inside this repository.
     */
    public @NotNull List<PsiMethod> getMethods() {
        return new ArrayList<>(methods.keySet());
    }

    /**
     * Returns a {@link MethodInfo} that corresponds to a given {@link PsiMethod}.
     *
     * @param psiMethod a {@link PsiMethod} to get {@link MethodInfo} for.
     * @return {@link Optional} that contains {@link MethodInfo} for a given {@link PsiMethod} or
     *         empty if given method hasn't been put to this repository.
     */
    public @NotNull Optional<MethodInfo> getMethodInfo(final @NotNull PsiMethod psiMethod) {
        MethodInfo info = methods.get(psiMethod);

        if (info == null) {
            return Optional.empty();
        }

        return Optional.of(info);
    }

    public static class Builder {
        private final @NotNull Map<PsiMethod, MethodInfo.Builder> builders = new HashMap<>();

        public @NotNull MethodInfoRepository build() {
            return new MethodInfoRepository(
                builders.entrySet()
                       .stream()
                       .collect(Collectors.toMap(Map.Entry::getKey, it -> it.getValue().build()))
            );
        }

        public void addMethod(final @NotNull PsiMethod psiMethod) {
            builders.computeIfAbsent(psiMethod, MethodInfo.Builder::new);
        }

        public void addMethodCall(
            final @NotNull PsiMethod caller,
            final @NotNull PsiMethod target,
            final @NotNull PsiMethodCallExpression expression
        ) {
            builders.computeIfAbsent(caller, MethodInfo.Builder::new)
                    .addTarget(target, expression);

            builders.computeIfAbsent(target, MethodInfo.Builder::new)
                    .addCaller(caller, expression);
        }

        public void addFieldAccess(final @NotNull PsiMethod method, final @NotNull PsiField field) {
            builders.computeIfAbsent(method, MethodInfo.Builder::new).addFieldAccess(field);
        }
    }
}
