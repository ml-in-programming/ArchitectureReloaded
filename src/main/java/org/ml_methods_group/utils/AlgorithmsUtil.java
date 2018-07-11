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

package org.ml_methods_group.utils;

import org.ml_methods_group.algorithm.entity.OldEntity;

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

    public static Entry<String, Long> getDominantClass(Collection<OldEntity> entities) {
        return entities.stream()
                .collect(Collectors.groupingBy(OldEntity::getClassName, Collectors.counting()))
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
