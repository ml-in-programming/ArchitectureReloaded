package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsProtectedMethodMetric;

public class NumProtectedMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumProtectedMethodsCalculator() {
        super(new IsProtectedMethodMetric());
    }
}
