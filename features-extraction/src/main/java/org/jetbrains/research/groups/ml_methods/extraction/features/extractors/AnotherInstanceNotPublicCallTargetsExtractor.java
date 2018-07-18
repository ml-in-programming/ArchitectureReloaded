package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.AnotherInstanceNotPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class AnotherInstanceNotPublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull AnotherInstanceNotPublicCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new AnotherInstanceNotPublicCallTargets(
            (int) methodInfo.getAnotherInstanceTargets(
                MethodFilters.sameClass(methodInfo.getContainingClass()),
                MethodFilters.isNotPublic,
                MethodFilters.isNotStatic
            ).count()
        );
    }
}
