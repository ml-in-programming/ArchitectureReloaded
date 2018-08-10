package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.IsAbstractMetric;

public class IsAbstractMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new IsAbstractMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNotAbstractClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testAbstractWithoutAbstractMethods() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
    }
}
