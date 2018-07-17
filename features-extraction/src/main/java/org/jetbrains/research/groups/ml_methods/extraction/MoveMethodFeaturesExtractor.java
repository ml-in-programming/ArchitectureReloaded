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

    /**
     * Extract features for a given list of refactorings.
     *
     * @param scope a scope of files to work in.
     * @param refactorings a {@link List} of "move method" refactorings to extract features for.
     * @param extractors extractors which will be used to extract each feature.
     * @return a {@link List} of {@link FeatureVector} for each {@link Refactoring}. It is
     * guaranteed that features will be in the same order as refactorings.
     */
    public @NotNull List<FeatureVector> extract(
        final @NotNull AnalysisScope scope,
        final @NotNull List<Refactoring> refactorings,
        final @NotNull List<Class<? extends MoveMethodFeaturesExtractor>> extractors
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
