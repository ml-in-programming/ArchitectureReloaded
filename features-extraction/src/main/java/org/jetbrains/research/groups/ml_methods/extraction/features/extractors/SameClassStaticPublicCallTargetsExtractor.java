package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameClassStaticPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstanceNotPublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.features.SameInstancePublicCallTargets;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.jetbrains.research.groups.ml_methods.utils.MethodUtils;

public class SameClassStaticPublicCallTargetsExtractor implements MoveMethodSingleFeatureExtractor {
    @Override
    public SameClassStaticPublicCallTargets extract(
        @NotNull MethodInfo methodInfo,
        @NotNull PsiClass targetClass
    ) {
        return new SameClassStaticPublicCallTargets(
            (int) methodInfo.getAnotherInstanceTargets(
                Filters.sameClass(methodInfo.getContainingClass()),
                Filters.isPublic,
                Filters.isStatic
            ).count()
        );
    }
}
