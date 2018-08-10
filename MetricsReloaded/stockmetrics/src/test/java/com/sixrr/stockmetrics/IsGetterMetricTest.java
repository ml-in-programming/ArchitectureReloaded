package com.sixrr.stockmetrics;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodMetrics.IsGetterMetric;

public class IsGetterMetricTest extends MetricAbstractTest {
    private final static Metric METRIC = new IsGetterMetric();

    public void testGetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.getIntField()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.getListField()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.isBooleanField()"));
        assertEquals(1.0, metricResults.getMethodMetric(METRIC, "A.hasField()"));
    }

    public void testNotGetters() {
        MetricsResultsHolderTestImpl metricResults = runMetricOnTestCase(METRIC);
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.getNotGetterBecauseOfVoid()"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.notGetterBecauseOfName()"));
        assertEquals(0.0, metricResults.getMethodMetric(METRIC, "A.getNotGetterBecauseOfParameter(int)"));
    }
}
