package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumFinalFieldsMetric;

public class NumFinalFieldsMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumFinalFieldsMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(3.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNestedClasses() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(3.0, metricResults.getClassMetric(METRIC, "A.C"));
    }

    public void testExtendsAbstractClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(4.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(3.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "A.C"));
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "B"));
    }
}
