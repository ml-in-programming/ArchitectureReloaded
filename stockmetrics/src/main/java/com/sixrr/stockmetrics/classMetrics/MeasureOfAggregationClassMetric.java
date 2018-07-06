/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sixrr.stockmetrics.classMetrics;

import com.sixrr.metrics.MetricCalculator;
import com.sixrr.metrics.MetricType;
import com.sixrr.stockmetrics.classCalculators.MeasureOfAggregationClassCalculator;
import org.jetbrains.annotations.NotNull;

public class MeasureOfAggregationClassMetric extends ClassMetric {
    /**
     * The user-visible name of the metric.  This need not be unique globally, but should be unique within a metric category
     *
     * @return the display name for the metric.
     */
    @NotNull
    @Override
    public String getDisplayName() {
        return "Measure of Aggregation";
    }

    /**
     * The user-visible abbreviation of the metric.  This need not be unique globally, but should be unique within a metric category
     *
     * @return the abbreviation for the metric.
     */
    @NotNull
    @Override
    public String getAbbreviation() {
        return "MOA";
    }

    /**
     * The type of the metric, indicating whether the number returned is a score, a count, or an average.
     *
     * @return the metric type
     */
    @NotNull
    @Override
    public MetricType getType() {
        return MetricType.Count;
    }

    /**
     * Create a calculator for this method.  The calculator returned is used for the duration of one entire metrics run.
     *
     * @return a calculator for this metric.
     */
    @NotNull
    @Override
    public MetricCalculator createCalculator() {
        return new MeasureOfAggregationClassCalculator();
    }
}
