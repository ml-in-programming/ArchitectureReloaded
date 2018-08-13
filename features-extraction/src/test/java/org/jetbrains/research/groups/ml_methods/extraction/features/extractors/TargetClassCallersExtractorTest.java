package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TargetClassCallersExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyLists() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void noTargetClassCallers() throws Exception {
        mockAnotherInstanceCallers(
            mockPsiMethod(anotherClass),
            mockPsiMethod(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void sameInstanceCaller() throws Exception {
        mockAnotherInstanceCallers(
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassCallers() throws Exception {
        mockAnotherInstanceCallers(
            mockPsiMethod(targetClass),
            mockPsiMethod(targetClass)
        );

        assertExtractedFeatureIs(2.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new TargetClassCallersExtractor();
    }
}