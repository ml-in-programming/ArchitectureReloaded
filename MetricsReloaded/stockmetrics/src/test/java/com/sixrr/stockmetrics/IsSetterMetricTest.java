package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.IsSetterMetric;

public class IsSetterMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new IsSetterMetric();

    public void testSetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.setIntField(int)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.setListField(List<Double>)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.setBooleanField(boolean)"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.setHasField(boolean)"));
    }

    public void testNotSetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.setNotSetterBecauseOf0Params()"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.setNotSetterBecauseOfMoreThan1Params(int,int)"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.setNotSetterBecauseOfReturnType(int)"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.notSetterBecauseOfName(int)"));
    }
}
