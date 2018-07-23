package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SameInstanceCallersExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyLists() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void sameInstanceCaller() throws Exception {
        mockSameInstanceCallers(
            mockPsiMethod(containingClass),
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(2.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new SameInstanceCallersExtractor();
    }
}