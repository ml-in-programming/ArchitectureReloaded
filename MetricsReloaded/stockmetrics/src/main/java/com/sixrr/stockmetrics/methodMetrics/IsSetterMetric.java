package com.sixrr.stockmetrics.methodMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.i18n.StockMetricsBundle;
import com.sixrr.stockmetrics.methodCalculators.IsSetterCalculator;
import org.jetbrains.annotations.NotNull;

public class IsSetterMetric extends MethodMetric {
    @NotNull
    @Override
    public String getDisplayName() {
        return StockMetricsBundle.message("is.setter.display.name");
    }

    @NotNull
    @Override
    public String getAbbreviation() {
        return StockMetricsBundle.message("is.setter.abbreviation");
    }

    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Count;
    }

    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new IsSetterCalculator();
    }
}