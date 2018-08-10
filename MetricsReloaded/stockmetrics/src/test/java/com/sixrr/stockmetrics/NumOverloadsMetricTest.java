package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumOverloadsMetric;

public class NumOverloadsMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumOverloadsMetric();

    public void testSimple() {
        Metric metric = new NumOverloadsMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA1()"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA1(int)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA1(double)"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA2()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA3(int,double)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA3(double,int)"));
    }

    public void testSupers() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "ClassA.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "ClassB.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "ClassB.methodA1(double)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, "ClassC.methodA1(double)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, "ClassC.methodA1(int)"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, "ClassD.methodA1(int)"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, "ClassD.methodA1()"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, "ClassD.methodA1(int,double)"));
    }
}
