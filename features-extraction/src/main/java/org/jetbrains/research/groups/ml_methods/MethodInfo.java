package org.jetbrains.research.groups.ml_methods;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

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
     * Returns number of times given invoker calls method this {@link MethodInfo} object is created
     * for.
     */
    public @NotNull Integer getOutsideInvocations(
        final @NotNull PsiMethod invoker
    ) {
        return outsideInvocations.getOrDefault(invoker, 0);
    }

    /**
     * Returns number of times method this {@link MethodInfo} object is created for calls given
     * target method.
     */
    public @NotNull Integer getInsideCalls(final @NotNull PsiMethod target) {
        return insideCalls.getOrDefault(target, 0);
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
