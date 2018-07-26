package com.sixrr.stockmetrics;

import com.intellij.analysis.AnalysisScope;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCalculator;

import java.util.Collections;

public abstract class MetricAbstractTest extends LightCodeInsightFixtureTestCase {

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases";
    }

    private AnalysisScope createScope(String pathToTestCase) {
        return new AnalysisScope(myFixture.getProject(), Collections.singletonList(myFixture.copyDirectoryToProject(pathToTestCase, pathToTestCase)));
    }

    MetricsResultsHolderTestImpl runMetricOnTestCase(Metric metric, String pathToTestCase) {
        MetricCalculator metricCalculator = metric.createCalculator();
        MetricsResultsHolderTestImpl metricsResults = new MetricsResultsHolderTestImpl();
        metricCalculator.beginMetricsRun(metric, metricsResults, null);
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
}