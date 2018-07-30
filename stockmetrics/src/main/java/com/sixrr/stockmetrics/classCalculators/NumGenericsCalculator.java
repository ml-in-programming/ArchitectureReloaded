package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsGenericMetric;

public class NumGenericsCalculator extends SummarizeMethodMetricsCalculator {
    public NumGenericsCalculator() {
        super(new IsGenericMetric());
    }
}
