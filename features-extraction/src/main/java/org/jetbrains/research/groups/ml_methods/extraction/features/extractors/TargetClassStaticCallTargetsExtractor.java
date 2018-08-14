package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassStaticCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class TargetClassStaticCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull
    TargetClassStaticCallTargets extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new TargetClassStaticCallTargets(
            methodInfo.getAnotherInstanceTargets(
                MethodFilters.sameClass(targetClass),
                MethodFilters.isStatic
            ).count()
        );
    }
}
