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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Kivi on 04.04.2017.
 */
public abstract class Entity {

    public Entity(String entity_name, MetricsRunImpl metricsRun) {
        name = entity_name;
        vector = initializeVector(metricsRun);
    }

    public static final int Dimension = 5;

    abstract MetricCategory getCategory();

    public Double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    private Double[] vector;
    private String name;

    protected abstract Double[] initializeVector(MetricsRunImpl metricsRun);

    protected static final Map<String, Integer> components;
    static {
        Map<String, Integer> comps = new HashMap<String, Integer>();
        comps.put("DIT", 1);
        comps.put("NOC", 2);
        comps.put("FIC", 3);
        comps.put("FOC", 4);
        comps.put("FIM", 3);
        comps.put("FOM", 4);
        components = Collections.unmodifiableMap(comps);
    }
}
