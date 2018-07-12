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

package org.jetbrains.research.groups.ml_methods.utils;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsProfilesUtil {
    public static MetricsProfile createProfile(String name, Collection<Class<? extends Metric>> requestedMetrics) {
        final List<MetricInstance> metrics = new ArrayList<>();
        for (Class<? extends Metric> metricClass : requestedMetrics) {
            try {
                MetricInstance instance = new MetricInstanceImpl(metricClass.newInstance());
                instance.setEnabled(true);
                metrics.add(instance);
            } catch (Exception e) {
                System.out.println("Failed to create metric for name: " + metricClass.getCanonicalName());
            }
        }
        return new MetricsProfileImpl(name, metrics);
    }

    public static boolean checkMetricsList(String profileName, Set<Class<? extends Metric>> expected,
                                           MetricsProfileRepository repository) {
        final MetricsProfile refactoringProfile = repository.getProfileForName(profileName);
        if (refactoringProfile == null) {
            return false;
        }
        Set<Class<? extends Metric>> currentSet = refactoringProfile.getMetricInstances()
                .stream()
                .map(MetricInstance::getMetric)
                .map(Metric::getClass)
                .collect(Collectors.toSet());
        return expected.equals(currentSet);
    }

    public static void removeProfileForName(String name, MetricsProfileRepository repository) {
        final MetricsProfile profile = repository.getProfileForName(name);
        if (profile != null) {
            repository.deleteProfile(profile);
        }
    }
}
