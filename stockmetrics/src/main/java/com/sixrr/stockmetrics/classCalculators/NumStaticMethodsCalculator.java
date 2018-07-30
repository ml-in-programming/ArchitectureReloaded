package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsStaticMethodMetric;

public class NumStaticMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumStaticMethodsCalculator() {
        super(new IsStaticMethodMetric());
    }
}