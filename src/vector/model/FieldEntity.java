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
import java.util.HashSet;


/**
 * Created by Kivi on 04.04.2017.
 */
public class FieldEntity extends Entity {
    public FieldEntity(String entity_name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        super(entity_name, metricsRun, propertiesFinder);
        RelevantProperties rp = propertiesFinder.getProperties(entity_name);
        double temp = rp.numberOfMethods();
        System.out.println(temp);
        vector[3] = Double.valueOf(temp);
    }

    public MetricCategory getCategory() {
        return MetricCategory.Package;
    }

    protected Double[] initializeVector(MetricsRunImpl metricsRun) {
        Double[] vector = new Double[Dimension];
        MetricsResult classResults = metricsRun.getResultsForCategory(MetricCategory.Class);
        String className = getClassName();
        for (Metric metric : metricsRun.getMetrics()) {
            if (metric.getCategory().equals(MetricCategory.Class)) {
                Integer id = components.get(metric.getAbbreviation());
                vector[id] = classResults.getValueForMetric(metric, className);
            }
        }

        vector[2] = 0.0;

        return vector;
    }

    protected HashSet<String> findRelevantProperties() {
        HashSet<String> properties = new HashSet<String>();
        properties.add(getName());
        properties.add(getClassName());

        return properties;
    }

    @Override
    public String getClassName() {
        String name = getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
