package com.sixrr.stockmetrics.classCalculators;

import com.sixrr.stockmetrics.methodMetrics.IsSetterMetric;

public class NumSettersCalculator extends SummarizeMethodMetricsCalculator {
    public NumSettersCalculator() {
        super(new IsSetterMetric());
    }
}
