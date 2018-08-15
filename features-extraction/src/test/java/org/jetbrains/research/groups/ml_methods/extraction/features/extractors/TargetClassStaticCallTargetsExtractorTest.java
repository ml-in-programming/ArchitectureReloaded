package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TargetClassStaticCallTargetsExtractorTest extends MoveMethodFeatureExtractorTest {
    @Test
    public void emptyList() throws Exception {
        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherClassTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(anotherClass),
            mockPsiMethod(anotherClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void anotherClassStaticTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(anotherClass, PsiModifier.STATIC),
            mockPsiMethod(anotherClass, PsiModifier.STATIC)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void containingClassTarget() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void containingClassStaticTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.STATIC),
            mockPsiMethod(containingClass, PsiModifier.STATIC)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassNonStaticTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(targetClass),
            mockPsiMethod(targetClass)
        );

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void targetClassStaticTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(targetClass, PsiModifier.STATIC),
            mockPsiMethod(targetClass, PsiModifier.STATIC)
        );

        assertExtractedFeatureIs(2.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new TargetClassStaticCallTargetsExtractor();
    }
}