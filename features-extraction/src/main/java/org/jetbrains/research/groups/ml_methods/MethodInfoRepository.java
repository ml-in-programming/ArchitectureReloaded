package org.jetbrains.research.groups.ml_methods;

import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is a repository of {@link MethodInfo}. It stores {@link MethodInfo} objects for each method
 * that has been encountered and not filtered out during analysis.
 */
public class MethodInfoRepository {
    private final @NotNull Map<PsiMethod, MethodInfo> methods;

    private MethodInfoRepository(final @NotNull Map<PsiMethod, MethodInfo> methods) {
        this.methods = methods;
    }

    /**
     * Returns a map from {@link PsiMethod} to a {@link MethodInfo} that corresponds to this method.
     */
    public @NotNull Map<PsiMethod, MethodInfo> getMethods() {
        return Collections.unmodifiableMap(methods);
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

        public void addMethodCall(
            final @NotNull PsiMethod caller,
            final @NotNull PsiMethod target
        ) {
            builders.computeIfAbsent(caller, it -> new MethodInfo.Builder())
                    .addInsideCall(target);

            builders.computeIfAbsent(target, it -> new MethodInfo.Builder())
                    .addOutsideInvocation(caller);
        }
    }
}
