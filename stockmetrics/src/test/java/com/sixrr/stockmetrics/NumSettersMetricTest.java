package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumSettersMetric;

public class NumSettersMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumSettersMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testOnlySetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(4.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNoSetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNestedClasses() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.C"));
    }

    public void testExtendsClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "B"));
    }
}
