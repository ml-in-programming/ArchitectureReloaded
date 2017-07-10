/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
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

package vector.model.entity;

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import vector.model.PropertiesFinder;

import java.util.Arrays;
import java.util.HashSet;

public class MethodEntity extends Entity {
    public MethodEntity(String name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        super(name, metricsRun, propertiesFinder);
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Method;
    }

    @Override
    protected double[] initializeVector(MetricsRunImpl metricsRun) {
        final double[] vector = new double[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = 0.0;
        }

        final MetricCategory category = getCategory();
        final MetricsResult results = metricsRun.getResultsForCategory(category);
        final MetricsResult classResults = metricsRun.getResultsForCategory(MetricCategory.Class);

        processEntity(getClassName(), MetricCategory.Class,  classResults, metricsRun, vector);
        processEntity(getName(), category, results, metricsRun, vector);

        return vector;
    }

    @Override
    protected HashSet<String> findRelevantProperties() {
        return new HashSet<>(Arrays.asList(getName(), getClassName()));
    }

    @Override
    public String getClassName() {
        final String signature = getName();
        final String name = signature.substring(0, signature.indexOf('('));
        return name.substring(0, name.lastIndexOf('.'));
    }
}
