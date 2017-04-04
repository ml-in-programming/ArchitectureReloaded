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

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;

import java.util.HashMap;

/**
 * Created by Kivi on 04.04.2017.
 */
public abstract class Entity {

    public Entity(String entity_name, MetricsRunImpl metricsRun) {
        vector = new Double[Dimension];
        name = entity_name;
        MetricCategory category = getCategory();
        MetricsResult results = metricsRun.getResultsForCategory(category);

        HashMap<String, Integer> components = new HashMap<String, Integer>();
        components.put("DIT", 1);
        components.put("NOC", 2);
        components.put("FIC", 3);
        components.put("FOC", 4);
        components.put("FIM", 3);
        components.put("FOM", 4);

        for (Metric metric : metricsRun.getMetrics()) {
            if (metric.getCategory().equals(category)) {
                Integer id = components.get(metric.getAbbreviation());
                vector[id] = results.getValueForMetric(metric, entity_name);
            }
        }
    }

    abstract MetricCategory getCategory();

    public Double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    private Double[] vector;
    private String name;

    public static final int Dimension = 5;
}
