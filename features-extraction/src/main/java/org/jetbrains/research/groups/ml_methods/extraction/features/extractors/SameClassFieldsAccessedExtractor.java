package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameClassFieldsAccessed;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;

public class SameClassFieldsAccessedExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public @NotNull SameClassFieldsAccessed extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        return new SameClassFieldsAccessed(
            methodInfo.getAccessedFields(
                FieldFilters.sameClass(methodInfo.getContainingClass())
            ).count()
        );
    }
}
