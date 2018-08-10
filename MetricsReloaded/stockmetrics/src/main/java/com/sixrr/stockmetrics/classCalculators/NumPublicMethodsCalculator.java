package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsPublicMethodMetric;

public class NumPublicMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumPublicMethodsCalculator() {
        super(new IsPublicMethodMetric());
    }
}
