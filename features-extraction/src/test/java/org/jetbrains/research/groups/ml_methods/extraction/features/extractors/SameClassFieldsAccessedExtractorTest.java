package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SameClassFieldsAccessedExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyList() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherClassFieldAccessed() throws Exception {
        mockAccessedFields(
            mockPsiField(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassFieldAccessed() throws Exception {
        mockAccessedFields(
            mockPsiField(targetClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void sameClassFieldAccessed() throws Exception {
        mockAccessedFields(
            mockPsiField(containingClass),
            mockPsiField(containingClass)
        );

        assertExtractedFeatureIs(2.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new SameClassFieldsAccessedExtractor();
    }
}
