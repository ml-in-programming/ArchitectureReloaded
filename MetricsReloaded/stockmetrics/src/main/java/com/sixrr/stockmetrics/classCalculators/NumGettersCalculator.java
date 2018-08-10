package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsGetterMetric;

public class NumGettersCalculator extends SummarizeMethodMetricsCalculator {
    public NumGettersCalculator() {
        super(new IsGetterMetric());
    }
}
