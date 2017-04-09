/*
 * Copyright 2005-2017 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package vector.model;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Kivi on 04.04.2017.
 */
public class ClassEntity extends Entity {
    public ClassEntity(String entity_name, MetricsRunImpl metricsRun) {
        super(entity_name, metricsRun);
    }

    public MetricCategory getCategory() {
        return MetricCategory.Class;
    }

    protected Double[] initializeVector(MetricsRunImpl metricsRun) {
        Double[] vector = new Double[Dimension];
        MetricCategory category = getCategory();
        MetricsResult results = metricsRun.getResultsForCategory(category);

        for (Metric metric : metricsRun.getMetrics()) {
            if (metric.getCategory().equals(category)) {
                Integer id = components.get(metric.getAbbreviation());
                vector[id] = results.getValueForMetric(metric, getName());
            }
        }

        return vector;
    }
}
