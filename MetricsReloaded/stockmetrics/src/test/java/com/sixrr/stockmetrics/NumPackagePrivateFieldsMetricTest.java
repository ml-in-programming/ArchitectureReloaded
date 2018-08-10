package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumPackagePrivateFieldsMetric;

public class NumPackagePrivateFieldsMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumPackagePrivateFieldsMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
    }

    public void testNestedClasses() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A.C"));
    }

    public void testExtendsAbstractClass() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "A"));
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A.B"));
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "A.C"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "B"));
    }
}
