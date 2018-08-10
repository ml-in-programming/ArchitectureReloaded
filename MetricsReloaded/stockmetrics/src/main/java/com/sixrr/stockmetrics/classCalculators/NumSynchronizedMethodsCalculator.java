package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsSynchronizedMethodMetric;

public class NumSynchronizedMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumSynchronizedMethodsCalculator() {
        super(new IsSynchronizedMethodMetric());
    }
}