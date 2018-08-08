package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.AnotherInstanceCallers;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassCallers;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class TargetClassCallersExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull TargetClassCallers extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new TargetClassCallers(
            methodInfo.getAnotherInstanceCallers(
                MethodFilters.sameClass(targetClass)
            ).count()
        );
    }
}
