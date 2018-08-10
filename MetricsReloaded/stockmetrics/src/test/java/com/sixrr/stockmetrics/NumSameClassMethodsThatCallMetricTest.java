package com.sixrr.stockmetrics;

import com.sixrr.stockmetrics.methodMetrics.NumSameClassMethodsThatCallMetric;
import org.jetbrains.annotations.NotNull;

public class NumSameClassMethodsThatCallMetricTest extends MetricAbstractTest {
    private final @NotNull NumSameClassMethodsThatCallMetric metric =
        new NumSameClassMethodsThatCallMetric();

    public void testAnotherClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method3()"));
    }

    public void testIndirectCall() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
    }

    public void testInnerMethod() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
    }

    public void testRecursiveCalls() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
    }

    public void testSameClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method3()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method4()"));
    }
}
