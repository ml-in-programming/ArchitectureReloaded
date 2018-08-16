package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TargetClassFieldsAccessedExtractorTest extends MoveMethodFeatureExtractorTest {
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
    public void targetClassFieldsAccessed() throws Exception {
        mockAccessedFields(
            mockPsiField(targetClass),
            mockPsiField(targetClass)
        );

        assertExtractedFeatureIs(2.);
    }

    @Test
    public void sameClassFieldAccessed() throws Exception {
        mockAccessedFields(
            mockPsiField(containingClass),
            mockPsiField(containingClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new TargetClassFieldsAccessedExtractor();
    }
}