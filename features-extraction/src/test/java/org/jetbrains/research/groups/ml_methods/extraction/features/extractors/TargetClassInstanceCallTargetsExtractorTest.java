package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TargetClassInstanceCallTargetsExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyList() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void noTargetClassTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(anotherClass),
            mockPsiMethod(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void containingClassTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(targetClass),
            mockPsiMethod(targetClass)
        );

        assertExtractedFeatureIs(2.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new TargetClassInstanceCallTargetsExtractor();
    }
}