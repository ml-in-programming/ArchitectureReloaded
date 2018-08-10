package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsFinalMethodMetric;

public class NumFinalMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumFinalMethodsCalculator() {
        super(new IsFinalMethodMetric());
    }
}