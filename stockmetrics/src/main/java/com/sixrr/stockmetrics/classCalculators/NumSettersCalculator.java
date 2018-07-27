package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodCalculators.MethodCalculator;
import com.sixrr.stockmetrics.methodMetrics.IsSetterMetric;

public class NumSettersCalculator extends SummarizeMethodMetricsCalculator {
    private final static Metric methodMetric = new IsSetterMetric();
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
