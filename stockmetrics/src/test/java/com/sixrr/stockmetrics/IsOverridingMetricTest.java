package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.IsOverridingMetric;

public class IsOverridingMetricTest extends MetricAbstractTest {
    public void testSimple() {
        Metric metric = new IsOverridingMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);
        assertEquals(0.0, metricResults.getMethodMetric(metric, "ClassB.methodA2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "ClassA.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "ClassA.methodA2()"));
    }

    public void testInterface() {
        String aPackage = "isOverridingMetric.interface";
        Metric metric = new IsOverridingMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);
        assertEquals(0.0, metricResults.getMethodMetric(metric, "InterfaceB.methodA()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "ClassA.methodA()"));
    }
}

