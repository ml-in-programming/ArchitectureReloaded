package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.AnotherInstancePublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class AnotherInstancePublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull AnotherInstancePublicCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new AnotherInstancePublicCallTargets(
            methodInfo.getAnotherInstanceTargets(
                MethodFilters.sameClass(methodInfo.getContainingClass()),
                MethodFilters.isPublic,
                MethodFilters.isNotStatic
            ).count()
        );
    }
}
