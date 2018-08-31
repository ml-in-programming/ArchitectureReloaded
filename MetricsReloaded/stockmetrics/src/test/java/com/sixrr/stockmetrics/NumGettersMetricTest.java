package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumGettersMetric;

public class NumGettersMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumGettersMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testOnlyGetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(3.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNoGetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNestedClasses() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.C"));
    }

    public void testExtendsClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "B"));
    }
}
