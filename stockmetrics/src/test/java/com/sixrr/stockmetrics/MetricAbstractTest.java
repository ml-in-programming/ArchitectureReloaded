package com.sixrr.stockmetrics;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.Collections;

public abstract class MetricAbstractTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases";
    }

    private AnalysisScope createScope(String pathToTestCase) {
        return new AnalysisScope(myFixture.getProject(), Collections.singletonList(myFixture.copyDirectoryToProject(pathToTestCase, pathToTestCase)));
    }

    protected MetricsResultsHolderTestImpl runMetricOnTestCase(Metric metric) {
        MetricCalculator metricCalculator = metric.createCalculator();
        MetricsResultsHolderTestImpl metricsResults = new MetricsResultsHolderTestImpl();
        metricCalculator.beginMetricsRun(metric, metricsResults, null);

        String pathToTestCase = Paths.get(getTestClassName(), getTestName(true)).toString();
        AnalysisScope scope = createScope(pathToTestCase);
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