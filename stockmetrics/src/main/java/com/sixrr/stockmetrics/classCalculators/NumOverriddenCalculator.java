package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.methodCalculators.MethodCalculator;
import com.sixrr.stockmetrics.methodMetrics.NumOverridesMethodMetric;

import static java.lang.Math.min;

public class NumOverriddenCalculator extends SummarizeMethodMetricsCalculator {
    private final static Metric methodMetric = new NumOverridesMethodMetric();
    private final static MethodCalculator methodCalculator = (MethodCalculator) methodMetric.createCalculator();

    @Override
    protected Metric getMethodMetric() {
        return methodMetric;
    }

    @Override
    protected MethodCalculator getMethodCalculator() {
        return methodCalculator;
    }

    @Override
    protected double accumulate(double accumulated, double newValue) {
        return (accumulated + min(newValue, 1));
    }
}
