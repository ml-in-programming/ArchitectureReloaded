package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.NumOverridesMethodMetric;

import static java.lang.Math.min;

public class NumOverriddenCalculator extends SummarizeMethodMetricsCalculator {
    public NumOverriddenCalculator() {
        super(new NumOverridesMethodMetric());
    }

    @Override
    protected double accumulate(double accumulated, double newValue) {
        return (accumulated + min(newValue, 1));
    }
}
