package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassCallers;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class TargetClassCallersExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull TargetClassCallers extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new TargetClassCallers(
            methodInfo.getAnotherInstanceCallers()
                .stream()
                .filter(MethodFilters.sameClass(targetClass))
                .count()
        );
    }
}
