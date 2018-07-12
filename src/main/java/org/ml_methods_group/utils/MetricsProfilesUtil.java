package org.ml_methods_group.utils;

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
