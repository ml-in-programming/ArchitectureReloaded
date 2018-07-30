package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsPrivateMethodMetric;

public class NumPrivateMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumPrivateMethodsCalculator() {
        super(new IsPrivateMethodMetric());
    }
}
