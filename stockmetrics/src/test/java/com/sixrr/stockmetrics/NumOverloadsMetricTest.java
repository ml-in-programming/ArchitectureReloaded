package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.NumOverloadsMetric;

public class NumOverloadsMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new NumOverloadsMetric();

    public void testSimpleTest() {
        String aPackage = "numOverloadsMetric.simpleTest";
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC, aPackage.replace('.', '/'));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA1()"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA1(int)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA1(double)"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA2()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA3(int,double)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA3(double,int)"));
    }

    public void testSupers() {
        String aPackage = "numOverloadsMetric.testSupers";
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC, aPackage.replace('.', '/'));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassA.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassB.methodA1()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassB.methodA1(double)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassC.methodA1(double)"));
        assertEquals(2.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassC.methodA1(int)"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassD.methodA1(int)"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassD.methodA1()"));
        assertEquals(3.0, metricResults.getMethodMetric(METRIC, aPackage + ".ClassD.methodA1(int,double)"));
    }
}
