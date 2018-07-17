package org.jetbrains.research.groups.ml_methods.extraction.features.vector;

import com.intellij.psi.PsiClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.features.extractors.MoveMethodFeatureExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Objects of this class create {@link FeatureVector} objects for "move method" refactorings by
 * extracting all features with the use of {@link MoveMethodFeatureExtractor} objects that they
 * store.
 */
public class MoveMethodVectorExtractor {
    private final @NotNull List<MoveMethodFeatureExtractor> featureExtractors;

    public MoveMethodVectorExtractor(
        final @NotNull List<MoveMethodFeatureExtractor> featureExtractors
    ) {
        this.featureExtractors = new ArrayList<>(featureExtractors);
    }

    public @NotNull FeatureVector extract(
        final @NotNull MethodInfo methodInfo,
        final @NotNull PsiClass targetClass
    ) {
        List<Feature> features = new ArrayList<>();

        for (MoveMethodFeatureExtractor extractor : featureExtractors) {
            features.add(extractor.extract(methodInfo, targetClass));
        }

        return new FeatureVector(features);
    }
}
