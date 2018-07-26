package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumCalledMethodsMetric;

public class NumCalledMethodsMetricTest extends MetricAbstractTest {
    private final Metric metric = new NumCalledMethodsMetric();

    public void testSameClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numCalledMethodsMetric/sameClassMethods/");

        assertEquals(2.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameClassMethods.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameClassMethods.A.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameClassMethods.A.method3()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameClassMethods.A.method4()"));
    }

    public void testRecursiveCalls() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numCalledMethodsMetric/recursiveCalls/");

        assertEquals(1.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.recursiveCalls.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.recursiveCalls.A.method2()"));
    }

    public void testAnotherClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numCalledMethodsMetric/anotherClassMethods/");

        assertEquals(2.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.anotherClassMethods.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.anotherClassMethods.B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.anotherClassMethods.B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.anotherClassMethods.B.method3()"));
    }

    public void testSameMethodTwice() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numCalledMethodsMetric/sameMethodTwice/");

        assertEquals(3.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameMethodTwice.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameMethodTwice.B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameMethodTwice.B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numCalledMethodsMetric.sameMethodTwice.B.method3()"));
    }
}
