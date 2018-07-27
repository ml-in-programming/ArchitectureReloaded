package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodCalculators.MethodCalculator;
import com.sixrr.stockmetrics.methodMetrics.IsGetterMetric;

public class NumGettersCalculator extends SummarizeMethodMetricsCalculator {
    private final static Metric methodMetric = new IsGetterMetric();
    private final static MethodCalculator methodCalculator = (MethodCalculator) methodMetric.createCalculator();

    @Override
    protected Metric getMethodMetric() {
        return methodMetric;
    }

    @Override
    protected MethodCalculator getMethodCalculator() {
        return methodCalculator;
    }
}
