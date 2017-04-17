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
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Kivi on 04.04.2017.
 */
public abstract class Entity {

    public Entity(String entity_name, MetricsRunImpl metricsRun, PropertiesFinder propertiesFinder) {
        name = entity_name;
        vector = initializeVector(metricsRun);
        relevantProperties = propertiesFinder.getProperties(name);
    }

    public static final int Dimension = 4;

    abstract MetricCategory getCategory();

    public Double[] getVector() {
        return vector;
    }

    public String getName() {
        return name;
    }

    private Double[] vector;
    private RelevantProperties relevantProperties;
    private String name;

    protected abstract Double[] initializeVector(MetricsRunImpl metricsRun);
    protected abstract HashSet<String> findRelevantProperties();

    protected static final Map<String, Integer> components;
    static {
        Map<String, Integer> comps = new HashMap<String, Integer>();
        comps.put("DIT", 0);
        comps.put("NOC", 1);
        comps.put("FIC", 2);
        comps.put("FOC", 3);
        comps.put("FIM", 2);
        comps.put("FOM", 3);
        components = Collections.unmodifiableMap(comps);
    }

    public void print() {
        System.out.println(name + ": " + getCategory().name());
        System.out.print("    ");
        for (Double comp : vector) {
            System.out.print(comp);
            System.out.print(" ");
        }
        System.out.println();
        if (name.equals("startOverAgain.seriousMode.CompGetUp()")) {
            System.out.println("here");
        }
        relevantProperties.printAll();
    }
}
