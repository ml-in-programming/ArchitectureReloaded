package com.sixrr.stockmetrics.classMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.classCalculators.NumMeaningfulClassesClassCalculator;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import org.jetbrains.annotations.NotNull;

public class NumMeaningfulClassesClassMetric extends ClassMetric {
    @NotNull
    @Override
    public String getDisplayName() {
        return StockMetricsBundle.message("number.of.meaningful.classes.for.class.display.name");
    }

    @NotNull
    @Override
    public String getAbbreviation() {
        return StockMetricsBundle.message("number.of.meaningful.classes.for.class.abbreviation");
    }

    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Score;
    }

    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new NumMeaningfulClassesClassCalculator();
    }
}
