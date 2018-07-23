package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class AnotherInstanceCallersExtractorTest extends MoveMethodFeatureExtractorTest{
    @Test
    public void emptyLists() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void noAnotherInstanceCallers() throws Exception {
        mockAnotherInstanceCallers(
            mockPsiMethod(anotherClass),
            mockPsiMethod(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new AnotherInstanceCallersExtractor();
    }
}
