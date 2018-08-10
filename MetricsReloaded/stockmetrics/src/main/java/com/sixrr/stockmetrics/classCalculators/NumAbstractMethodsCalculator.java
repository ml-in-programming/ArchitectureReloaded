package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsAbstractMethodMetric;

public class NumAbstractMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumAbstractMethodsCalculator() {
        super(new IsAbstractMethodMetric());
    }
}