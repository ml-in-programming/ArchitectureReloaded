package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameClassStaticPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class SameClassStaticPublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull SameClassStaticPublicCallTargets extract(
        @NotNull MethodInfo methodInfo,
        @NotNull PsiClass targetClass
    ) {
        return new SameClassStaticPublicCallTargets(
            methodInfo.getAnotherInstanceTargets()
                .stream()
                .filter(MethodFilters.sameClass(methodInfo.getContainingClass()))
                .filter(MethodFilters.isPublic)
                .filter(MethodFilters.isStatic)
                .count()
        );
    }
}
