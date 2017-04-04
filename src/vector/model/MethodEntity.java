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
import com.sixrr.metrics.metricModel.MetricsRunImpl;

import java.util.List;

/**
 * Created by Kivi on 04.04.2017.
 */
public class MethodEntity extends Entity {
    public MethodEntity(String entity_name, MetricsRunImpl metricsRun) {
        super(entity_name, metricsRun);
    }

    public MetricCategory getCategory() {
        return MetricCategory.Method;
    }
}
