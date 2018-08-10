package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumOverriddenMetric;

public class NumOverriddenMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumOverriddenMetric();

    public void testSimple() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "ClassB"));
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "ClassA"));
    }

    public void testMultipleOverridden() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "ClassB"));
        assertEquals(2.0, metricResults.getClassMetric(METRIC, "ClassA"));
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "ClassC"));
    }

    public void notSupported_testInterface() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getClassMetric(METRIC, "ClassA"));
        assertEquals(1.0, metricResults.getClassMetric(METRIC, "InterfaceB"));
    }
}
