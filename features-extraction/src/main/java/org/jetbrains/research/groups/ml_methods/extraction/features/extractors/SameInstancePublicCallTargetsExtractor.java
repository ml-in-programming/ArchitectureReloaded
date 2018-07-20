package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstancePublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class SameInstancePublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull SameInstancePublicCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new SameInstancePublicCallTargets(
            methodInfo.getSameInstanceTargets(MethodFilters.isPublic).count()
        );
    }
}
