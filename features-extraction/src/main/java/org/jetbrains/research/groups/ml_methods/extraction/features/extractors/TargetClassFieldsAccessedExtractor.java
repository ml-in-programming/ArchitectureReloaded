package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.TargetClassFieldsAccessed;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class TargetClassFieldsAccessedExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull TargetClassFieldsAccessed extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new TargetClassFieldsAccessed(
            (int) methodInfo.getAccessedFields(
                FieldFilters.sameClass(targetClass)
            ).count()
        );
    }
}
