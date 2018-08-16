package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameClassStaticNotPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class SameClassStaticNotPublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull SameClassStaticNotPublicCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new SameClassStaticNotPublicCallTargets(
            methodInfo.getAnotherInstanceTargets().stream()
                .filter(MethodFilters.sameClass(methodInfo.getContainingClass()))
                .filter(MethodFilters.isNotPublic)
                .filter(MethodFilters.isStatic)
                .count()
        );
    }
}
