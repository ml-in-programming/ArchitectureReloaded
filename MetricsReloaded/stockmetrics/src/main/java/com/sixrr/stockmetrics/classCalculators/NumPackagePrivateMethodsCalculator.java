package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsPackagePrivateMethodMetric;

public class NumPackagePrivateMethodsCalculator extends SummarizeMethodMetricsCalculator {
    public NumPackagePrivateMethodsCalculator() {
        super(new IsPackagePrivateMethodMetric());
    }
}
