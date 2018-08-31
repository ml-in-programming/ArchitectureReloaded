package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SameInstanceNotPublicCallTargetsExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyList() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void sameInstancePublicTargets() throws Exception {
        mockSameInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.PUBLIC),
            mockPsiMethod(containingClass, PsiModifier.PUBLIC)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherInstanceAllModifiersTargets() throws Exception {
        mockSameInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.PUBLIC),
            mockPsiMethod(containingClass, PsiModifier.PROTECTED),
            mockPsiMethod(containingClass, PsiModifier.PRIVATE),
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(3.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new SameInstanceNotPublicCallTargetsExtractor();
    }
}
