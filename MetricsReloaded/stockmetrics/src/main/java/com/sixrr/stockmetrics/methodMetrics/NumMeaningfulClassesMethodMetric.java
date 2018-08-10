package com.sixrr.stockmetrics.methodMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import com.sixrr.stockmetrics.methodCalculators.NumMeaningfulClassesMethodCalculator;
import org.jetbrains.annotations.NotNull;

public class NumMeaningfulClassesMethodMetric extends MethodMetric {
    @NotNull
    @Override
    public String getDisplayName() {
        return StockMetricsBundle.message("number.of.meaningful.classes.for.method.display.name");
    }

    @NotNull
    @Override
    public String getAbbreviation() {
        return StockMetricsBundle.message("number.of.meaningful.classes.for.method.abbreviation");
    }

    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Score;
    }

    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new NumMeaningfulClassesMethodCalculator();
    }
}
