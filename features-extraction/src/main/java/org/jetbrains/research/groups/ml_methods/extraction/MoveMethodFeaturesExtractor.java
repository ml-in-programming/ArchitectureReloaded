package org.jetbrains.research.groups.ml_methods.extraction;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.FeatureVector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Collection;
import java.util.List;

public class MoveMethodFeaturesExtractor {
    private static final @NotNull MoveMethodFeaturesExtractor INSTANCE =
            new MoveMethodFeaturesExtractor();

    private MoveMethodFeaturesExtractor() {}

    public @NotNull MoveMethodFeaturesExtractor getInstance() {
        return INSTANCE;
    }

    public @NotNull List<FeatureVector> extract(
        final @NotNull AnalysisScope scope,
        final @NotNull Collection<Refactoring> refactorings,
        Collection<Class<? extends MoveMethodFeaturesExtractor>> extractors
    ) {
        throw new NotImplementedException();
    }

    public static class Refactoring {
        private final @NotNull PsiMethod method;

        private final @NotNull PsiClass targetClass;

        public Refactoring(final @NotNull PsiMethod method, final @NotNull PsiClass targetClass) {
            this.method = method;
            this.targetClass = targetClass;
        }

        public @NotNull PsiMethod getMethod() {
            return method;
        }

        public @NotNull PsiClass getTargetClass() {
            return targetClass;
        }
    }
}
