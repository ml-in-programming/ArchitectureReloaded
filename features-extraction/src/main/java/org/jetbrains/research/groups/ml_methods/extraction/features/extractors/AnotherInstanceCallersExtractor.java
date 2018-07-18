package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.AnotherInstanceCallers;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstanceCallers;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class AnotherInstanceCallersExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull AnotherInstanceCallers extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new AnotherInstanceCallers(
            (int) methodInfo.getAnotherInstanceCallers(
                MethodFilters.sameClass(methodInfo.getContainingClass())
            ).count()
        );
    }
}
