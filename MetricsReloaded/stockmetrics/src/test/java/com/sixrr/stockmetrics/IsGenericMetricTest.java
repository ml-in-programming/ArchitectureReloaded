package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.IsGenericMetric;

public class IsGenericMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new IsGenericMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.notGeneric()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.generic1(R)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.generic2(R)"));
    }

    public void testGenericClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.notGeneric()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.generic(R)"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.method(int)"));
    }

    public void testGenericInList() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.method(List<? super R>)"));
    }

    public void testNotUsedGeneric() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.method()"));
    }

    public void testComplexReturnType() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.method(int)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.method(R)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.method()"));
    }

    public void testTwoGenericTypes() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.method(T,R,Collection)"));
    }
}
