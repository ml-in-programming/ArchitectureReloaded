package org.jetbrains.research.groups.ml_methods.extraction.features.extractors;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.extraction.features.Feature;
import org.jetbrains.research.groups.ml_methods.extraction.info.MethodInfo;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public abstract class MoveMethodFeatureExtractorTest {
    @Mock
    private MethodInfo info;

    @Mock
    protected PsiClass containingClass;

    @Mock
    protected PsiClass targetClass;

    @Mock
    protected PsiClass anotherClass;

    @Before
    public void setUp() {
        doReturn(containingClass).when(info).getContainingClass();
    }

    protected abstract @NotNull MoveMethodSingleFeatureExtractor createExtractor();

    protected void assertExtractedFeatureIs(final double value) throws Exception {
        Feature feature = createExtractor().extract(info, targetClass);
        assertThat(feature.getValue(), is(equalTo(value)));
    }

    protected void mockAnotherInstanceCallers(final PsiMethod... methods) {
        doReturn(Arrays.asList(methods)).when(info).getAnotherInstanceCallers();
    }

    protected @NotNull PsiMethod mockPsiMethod(
        final @NotNull PsiClass containingClass,
        final String... modifiers
    ) {
        PsiMethod method = mock(PsiMethod.class);
        doReturn(containingClass).when(method).getContainingClass();

        for (String modifier : modifiers) {
            doReturn(true).when(method).hasModifierProperty(modifier);
        }

        return method;
    }
}
