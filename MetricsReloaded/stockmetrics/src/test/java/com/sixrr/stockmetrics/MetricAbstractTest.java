package com.sixrr.stockmetrics;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricsExecutionContext;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class MetricAbstractTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases";
    }

    private AnalysisScope createScope(String pathToTestCase) {
        return new AnalysisScope(myFixture.getProject(), Collections.singletonList(myFixture.copyDirectoryToProject(pathToTestCase, pathToTestCase)));
    }

    protected MetricsResultsHolderTestImpl runMetricOnTestCase(Metric metric) {
        String pathToTestCase = Paths.get(getTestClassName(), getTestName(true)).toString().replace("\\", "/");
        AnalysisScope scope = createScope(pathToTestCase);

        MetricCalculator metricCalculator = metric.createCalculator();
        MetricsResultsHolderTestImpl metricsResults = new MetricsResultsHolderTestImpl();

        MetricsExecutionContext executionContext = new MetricsExecutionContext() {
            private Map userData = new HashMap();

            @Override
            public final <T> T getUserData(@NotNull Key<T> key) {
                return (T) userData.get(key);
            }

            @Override
            public final <T> void putUserData(@NotNull Key<T> key, T t) {
                userData.put(key, t);
            }

            @Override
            public Project getProject() {
                return myFixture.getProject();
            }

            @Override
            public AnalysisScope getScope() {
                return scope;
            }
        };

        metricCalculator.beginMetricsRun(metric, metricsResults, executionContext);

        scope.accept(new PsiElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                metricCalculator.processFile(file);
            }
        });
        metricCalculator.endMetricsRun();
        return metricsResults;
    }

    private @NotNull String getTestClassName() {
        String name = this.getClass().getSimpleName();

        String testSuffix = "Test";
        if (name.endsWith(testSuffix)) {
            name = name.substring(0, name.length() - testSuffix.length());
        }

        char firstCharacter = Character.toLowerCase(name.charAt(0));
        return Character.toString(firstCharacter) + name.substring(1);
    }
}