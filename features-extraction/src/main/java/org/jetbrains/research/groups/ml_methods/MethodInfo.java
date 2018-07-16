package org.jetbrains.research.groups.ml_methods;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains structural information gathered for a particular method. It stores methods
 * that call this method or methods that this method calls etc.
 */
public class MethodInfo {
    private final @NotNull Map<PsiMethod, Integer> outsideInvocations;

    private final @NotNull Map<PsiMethod, Integer> insideCalls;

    private MethodInfo(
        final @NotNull Map<PsiMethod, Integer> outsideInvocations,
        final @NotNull Map<PsiMethod, Integer> insideCalls
    ) {
        this.outsideInvocations = outsideInvocations;
        this.insideCalls = insideCalls;
    }

    /**
     * Returns map of methods that call method this info object is created for. Each method is
     * mapped to a number of calls it makes.
     */
    public @NotNull Map<PsiMethod, Integer> getOutsideInvocations() {
        return Collections.unmodifiableMap(outsideInvocations);
    }

    /**
     * Returns map of methods that method this info object is created for calls. Each method is
     * mapped to a number of times it's called.
     */
    public @NotNull Map<PsiMethod, Integer> getInsideCalls() {
        return Collections.unmodifiableMap(insideCalls);
    }

    public static class Builder {
        private final @NotNull Map<PsiMethod, Integer> outsideInvocations = new HashMap<>();

        private final @NotNull Map<PsiMethod, Integer> insideCalls = new HashMap<>();

        public @NotNull MethodInfo build() {
            return new MethodInfo(outsideInvocations, insideCalls);
        }

        public void addOutsideInvocation(final @NotNull PsiMethod psiMethod) {
            outsideInvocations.put(
                psiMethod,
                outsideInvocations.getOrDefault(psiMethod, 0) + 1
            );
        }

        public void addInsideCall(final @NotNull PsiMethod psiMethod) {
            insideCalls.put(
                psiMethod,
                insideCalls.getOrDefault(psiMethod, 0) + 1
            );
        }
    }
}
