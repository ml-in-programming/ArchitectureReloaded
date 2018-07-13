package org.jetbrains.research.groups.ml_methods.utils;

import org.jetbrains.research.groups.ml_methods.algorithm.entity.Entity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class AlgorithmsUtil {
    public static <K, V> Map<K, V> combineMaps(Map<K, V> first, Map<K, V> second) {
        if (first.size() < second.size()) {
            return combineMaps(second, first);
        }
        first.putAll(second);
        return first;
    }

    public static <V> List<V> combineLists(List<V> first, List<V> second) {
        if (first.size() < second.size()) {
            return combineLists(second, first);
        }
        first.addAll(second);
        return first;
    }

    public static Entry<String, Long> getDominantClass(Collection<Entity> entities) {
        return entities.stream()
                .collect(Collectors.groupingBy(Entity::getClassName, Collectors.counting()))
                .entrySet().stream()
                .max(Entry.comparingByValue())
                .orElse(null);
    }

    public static double getGapBasedAccuracyRating(double distance, double difference) {
        return difference == 0 ? 0 : Math.min(5 * difference / distance, 1);
    }

    public static double getDensityBasedAccuracyRating(long dominantClassCount, long communitySize) {
        return (double) dominantClassCount / communitySize;
    }
}
