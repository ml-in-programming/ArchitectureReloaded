/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algoritm.entity;

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRunImpl;
import org.ml_methods_group.algoritm.PropertiesFinder;
import org.ml_methods_group.algoritm.RelevantProperties;

import java.util.Arrays;
import java.util.HashSet;

public class FieldEntity extends Entity {
    public FieldEntity(String name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        super(name, metricsRun, propertiesFinder);
        final RelevantProperties rp = propertiesFinder.getProperties(name);
        vector[2] = (double) rp.numberOfMethods();
    }

    @Override
    public MetricCategory getCategory() {
        return MetricCategory.Package;
    }

    @Override
    protected double[] initializeVector(MetricsRunImpl metricsRun) {
        final double[] vector = new double[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            vector[i] = 0.0;
        }

        final MetricsResult classResults = metricsRun.getResultsForCategory(MetricCategory.Class);
        processEntity(getClassName(), MetricCategory.Class,  classResults, metricsRun, vector);
        vector[3] = 0.0;

        return vector;
    }

    @Override
    protected HashSet<String> findRelevantProperties() {
        return new HashSet<>(Arrays.asList(getName(), getClassName()));
    }

    @Override
    public String getClassName() {
        final String name = getName();
        return name.substring(0, name.lastIndexOf('.'));
    }
}
