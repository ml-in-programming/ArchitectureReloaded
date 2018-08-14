package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class AnotherInstanceNotPublicCallTargetsExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyList() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void noAnotherInstanceTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(anotherClass),
            mockPsiMethod(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassTarget() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(targetClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherInstancePublicTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.PUBLIC),
            mockPsiMethod(containingClass, PsiModifier.PUBLIC)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherInstanceAllModifiersTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.PUBLIC),
            mockPsiMethod(containingClass, PsiModifier.PROTECTED),
            mockPsiMethod(containingClass, PsiModifier.PRIVATE),
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(3.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new AnotherInstanceNotPublicCallTargetsExtractor();
    }
}