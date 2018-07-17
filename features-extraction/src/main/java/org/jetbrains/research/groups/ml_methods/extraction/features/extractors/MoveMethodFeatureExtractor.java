package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;

/**
 * An interface for "move method" refactoring feature extractor. It has
 * {@link MoveMethodFeatureExtractor#extract} method which receives all needed information and
 * creates some {@link Feature}.
 */
public interface MoveMethodFeatureExtractor {
    Feature extract(@NotNull MethodInfo methodInfo, @NotNull PsiClass targetClass);
}
