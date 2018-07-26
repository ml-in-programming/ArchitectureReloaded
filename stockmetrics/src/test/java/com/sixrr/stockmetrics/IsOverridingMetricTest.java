package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.IsOverridingMetric;

public class IsOverridingMetricTest extends MetricAbstractTest {
    public void testSimpleTest() {
        String aPackage = "isOverridingMetric.simpleTest";
        Metric metric = new IsOverridingMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric);
        assertEquals(0.0, metricResults.getMethodMetric(metric, aPackage + ".ClassB.methodA2()"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, aPackage + ".ClassA.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, aPackage + ".ClassA.methodA2()"));
    }

    public void testInterface() {
        String aPackage = "isOverridingMetric.testInterface";
        Metric metric = new IsOverridingMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, aPackage.replace('.', '/'));
        assertEquals(0.0, metricResults.getMethodMetric(metric, aPackage + ".InterfaceB.methodA()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, aPackage + ".ClassA.methodA()"));
    }
}

