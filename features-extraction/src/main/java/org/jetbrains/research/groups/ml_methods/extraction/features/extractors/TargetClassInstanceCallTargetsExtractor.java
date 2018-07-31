package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassInstanceCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class TargetClassInstanceCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull
    TargetClassInstanceCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new TargetClassInstanceCallTargets(
            methodInfo.getAnotherInstanceTargets(
                MethodFilters.sameClass(targetClass),
                MethodFilters.isNotStatic
            ).count()
        );
    }
}
