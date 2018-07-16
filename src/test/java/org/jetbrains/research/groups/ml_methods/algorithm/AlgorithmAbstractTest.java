package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public abstract class AlgorithmAbstractTest extends ScopeAbstractTest {
    protected final TestCasesCheckers testCasesChecker =
        new TestCasesCheckers(getAlgorithm().getDescriptionString());

    protected RefactoringExecutionContext createContext(
        AnalysisScope scope,
        Algorithm algorithm,
        Consumer<RefactoringExecutionContext> checker
    ) {
        MetricsProfile profile =
            MetricsProfilesUtil.createProfile("test_profile", algorithm.requiredMetrics());
        return new RefactoringExecutionContext(myFixture.getProject(), scope, profile,
                Collections.singletonList(algorithm), true,
                checker);
    }

    protected void executeTest(Consumer<RefactoringExecutionContext> checker, String... files) {
        AnalysisScope scope = createScope(files);
        createContext(scope, getAlgorithm(), checker).executeSynchronously();
    }

    protected abstract Algorithm getAlgorithm();
}
