package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsOverridingMetric;

public class NumOverridingCalculator extends SummarizeMethodMetricsCalculator {
    public NumOverridingCalculator() {
        super(new IsOverridingMetric());
    }
}
