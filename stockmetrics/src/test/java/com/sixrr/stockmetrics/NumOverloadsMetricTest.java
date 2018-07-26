package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumOverloadsMetric;

public class NumOverloadsMetricTest extends MetricAbstractTest {
    public void testSimpleTest() {
        Metric metric = new NumOverloadsMetric();
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(metric, "numOverloadsMetric/simpleTest/");
        assertEquals(2.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA1()"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA1(int)"));
        assertEquals(2.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA1(double)"));
        assertEquals(0.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA2()"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA3(int,double)"));
        assertEquals(1.0, metricResults.getMethodMetric(metric, "numOverloadsMetric.simpleTest.ClassA.methodA3(double,int)"));
    }
}
