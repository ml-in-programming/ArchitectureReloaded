package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class SameClassStaticPublicCallTargetsExtractorTest extends MoveMethodFeatureExtractorTest {
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
            mockPsiMethod(anotherClass, PsiModifier.STATIC, PsiModifier.PUBLIC),
            mockPsiMethod(anotherClass, PsiModifier.STATIC, PsiModifier.PROTECTED)
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
    public void targetClassStaticTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(targetClass, PsiModifier.STATIC, PsiModifier.PUBLIC),
            mockPsiMethod(targetClass, PsiModifier.STATIC, PsiModifier.PROTECTED)
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

        assertExtractedFeatureIs(0.);
    }

    @Test
    public void sameClassStaticAllModifiersTargets() throws Exception {
        mockAnotherInstanceTargets(
            mockPsiMethod(containingClass, PsiModifier.STATIC, PsiModifier.PUBLIC),
            mockPsiMethod(containingClass, PsiModifier.STATIC, PsiModifier.PROTECTED),
            mockPsiMethod(containingClass, PsiModifier.STATIC, PsiModifier.PRIVATE),
            mockPsiMethod(containingClass, PsiModifier.STATIC)
        );

        assertExtractedFeatureIs(1.);
    }

    @Override
    protected @NotNull MoveMethodSingleFeatureExtractor createExtractor() {
        return new SameClassStaticPublicCallTargetsExtractor();
    }
}
