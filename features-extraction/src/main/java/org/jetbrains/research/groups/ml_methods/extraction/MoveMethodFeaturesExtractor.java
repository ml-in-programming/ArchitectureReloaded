package org.jetbrains.research.groups.ml_methods.extraction;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.extraction.features.extractors.MoveMethodSingleFeatureExtractor;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.FeatureVector;
import org.jetbrains.research.groups.ml_methods.extraction.features.vector.MoveMethodVectorExtractor;
import org.jetbrains.research.groups.ml_methods.extraction.info.InfoCollector;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfoRepository;

import java.util.ArrayList;
import java.util.List;

public class MoveMethodFeaturesExtractor {
    private static final @NotNull MoveMethodFeaturesExtractor INSTANCE =
            new MoveMethodFeaturesExtractor();

    private MoveMethodFeaturesExtractor() {}

    public static @NotNull MoveMethodFeaturesExtractor getInstance() {
        return INSTANCE;
    }

    /**
     * Extract features for a given list of refactorings.
     *
     * @param scope a scope of files to work in.
     * @param refactorings a {@link List} of "move method" refactorings to extract features for.
     * @param extractorClasses extractors which will be used to extract each feature.
     * @return a {@link List} of {@link FeatureVector} for each {@link MoveMethodRefactoring}. It is
     * guaranteed that features will be in the same order as refactorings.
     */
    public @NotNull List<FeatureVector> extract(
        final @NotNull AnalysisScope scope,
        final @NotNull List<MoveMethodRefactoring> refactorings,
        final @NotNull List<Class<? extends MoveMethodSingleFeatureExtractor>> extractorClasses
    ) throws IllegalAccessException, InstantiationException {
        MethodInfoRepository repository = InfoCollector.getInstance().collectInfo(scope);

        List<MoveMethodSingleFeatureExtractor> extractors = new ArrayList<>();
        for (Class<? extends MoveMethodSingleFeatureExtractor> extractorClass : extractorClasses) {
            extractors.add(extractorClass.newInstance());
        }

        MoveMethodVectorExtractor extractor = new MoveMethodVectorExtractor(extractors);

        List<FeatureVector> vectors = new ArrayList<>();
        for (MoveMethodRefactoring refactoring : refactorings) {
            MethodInfo methodInfo =
                repository.getMethodInfo(refactoring.getMethod()).orElseThrow(
                    () -> new IllegalArgumentException(
                            "Refactoring of method which was not found in scope"
                    )
                );

            vectors.add(extractor.extract(methodInfo, refactoring.getTargetClass()));
        }

        return vectors;
    }
}
