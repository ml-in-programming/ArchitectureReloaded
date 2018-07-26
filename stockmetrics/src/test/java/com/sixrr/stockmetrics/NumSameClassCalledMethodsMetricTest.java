package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumSameClassCalledMethodsMetric;

public class NumSameClassCalledMethodsMetricTest extends MetricAbstractTest {
    private final Metric metric = new NumSameClassCalledMethodsMetric();

    public void testSameClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numSameClassCalledMethodsMetric/sameClassMethods/");

        assertEquals(2.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameClassMethods.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameClassMethods.A.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameClassMethods.A.method3()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameClassMethods.A.method4()"));
    }

    public void testRecursiveCalls() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numSameClassCalledMethodsMetric/recursiveCalls/");

        assertEquals(1.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.recursiveCalls.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.recursiveCalls.A.method2()"));
    }

    public void testAnotherClassMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numSameClassCalledMethodsMetric/anotherClassMethods/");

        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.anotherClassMethods.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.anotherClassMethods.B.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.anotherClassMethods.B.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.anotherClassMethods.B.method3()"));
    }

    public void testSameMethodTwice() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numSameClassCalledMethodsMetric/sameMethodTwice/");

        assertEquals(3.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameMethodTwice.A.method1()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameMethodTwice.A.method2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numSameClassCalledMethodsMetric.sameMethodTwice.A.method3()"));
    }
}
