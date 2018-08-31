package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumCalledMethodsMetric;

public class NumCalledMethodsMetricTest extends MetricAbstractTest {
    private final Metric metric = new NumCalledMethodsMetric();

    public void testSameClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method3()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method4()"));
    }

    public void testRecursiveCalls() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(1.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
    }

    public void testAnotherClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(2.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method3()"));
    }

    public void testSameMethodTwice() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(3.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "B.method3()"));
    }

    public void testInnerMethod() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);

        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "A.method2()"));
    }
}
