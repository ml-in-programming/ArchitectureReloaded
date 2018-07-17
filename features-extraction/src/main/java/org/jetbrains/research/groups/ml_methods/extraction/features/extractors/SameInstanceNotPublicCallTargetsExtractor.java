package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstanceNotPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.jetbrains.research.groups.ml_methods.utils.MethodUtils;

public class SameInstanceNotPublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public SameInstanceNotPublicCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new SameInstanceNotPublicCallTargets(
            (int) methodInfo.getSameInstanceTargets()
                            .stream()
                            .filter(it -> !MethodUtils.isPublic(it))
                            .count()
        );
    }
}
