package com.sixrr.stockmetrics.methodMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import com.sixrr.stockmetrics.methodCalculators.NumSameClassCalledMethodsCalculator;
import org.jetbrains.annotations.NotNull;

public class NumSameClassCalledMethodsMetric extends MethodMetric {
    @NotNull
    @Override
    public String getDisplayName() {
        return StockMetricsBundle.message("number.of.called.methods.of.same.class.display.name");
    }

    @NotNull
    @Override
    public String getAbbreviation() {
        return StockMetricsBundle.message("number.of.called.methods.of.same.class.abbreviation");
    }

    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Score;
    }

    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new NumSameClassCalledMethodsCalculator();
    }
}
