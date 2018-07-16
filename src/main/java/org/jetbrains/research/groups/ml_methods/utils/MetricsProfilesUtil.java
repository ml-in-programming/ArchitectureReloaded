package org.jetbrains.research.groups.ml_methods.utils;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.profile.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetricsProfilesUtil {
    public static MetricsProfile createProfile(String name, Collection<Metric> requestedMetrics) {
        final List<MetricInstance> metrics = new ArrayList<>();
        for (Metric metric : requestedMetrics) {
            MetricInstance instance = new MetricInstanceImpl(metric);
            instance.setEnabled(true);
            metrics.add(instance);
        }
        return new MetricsProfileImpl(name, metrics);
    }

    public static boolean checkMetricsList(String profileName, Set<Metric> expected,
                                           MetricsProfileRepository repository) {
        final MetricsProfile refactoringProfile = repository.getProfileForName(profileName);
        if (refactoringProfile == null) {
            return false;
        }
        Set<Metric> currentSet = refactoringProfile.getMetricInstances()
                .stream()
                .map(MetricInstance::getMetric)
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
