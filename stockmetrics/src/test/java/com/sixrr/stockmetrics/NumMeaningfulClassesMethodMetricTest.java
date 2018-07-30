package com.sixrr.stockmetrics;

import com.sixrr.stockmetrics.methodMetrics.NumMeaningfulClassesMethodMetric;
import org.jetbrains.annotations.NotNull;

public class NumMeaningfulClassesMethodMetricTest extends MetricAbstractTest {
    private final @NotNull
    NumMeaningfulClassesMethodMetric metric = new NumMeaningfulClassesMethodMetric();

    public void testReturnType() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method3()"));
    }

    public void ignoredTestThrows() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method3()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method4()"));
    }

    public void testMethodCall() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method3()"));
    }

    public void testFieldAccess() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method3()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method4()"));
    }

    public void testObjectInstantiation() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method3()"));
    }

    public void testLocalDeclaration() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method3()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method4()"));
    }
}
