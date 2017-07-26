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

import java.util.*;

public final class ListUtil {

    public static <T> void removeCopies(List<T> collection) {
        final Set<T> distinctValues = new HashSet<>(collection);
        collection.clear();
        collection.addAll(distinctValues);
    }

    /**
     * Expected, that lists are already sorted
     * @param first collection
     * @param second collection
     * @param comparator comparator, which was used to sort lists
     * @param <T> type or values
     * @return size of intersection
     */
    public static <T> int sizeOfIntersection(List<T> first, List<T> second, Comparator<T> comparator) {
        int intersection = 0;
        int firstIndex = 0;
        int secondIndex = 0;
        while (firstIndex < first.size() && secondIndex < second.size()) {
            int cmp = comparator.compare(first.get(firstIndex), second.get(secondIndex));
            if (cmp == 0) {
                intersection++;
                firstIndex++;
                secondIndex++;
            } else if (cmp < 0) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }
        return intersection;
    }

    /**
     * Expected, that lists are already sorted
     * @param first collection
     * @param second collection
     * @param comparator comparator, which was used to sort lists
     * @param <T> type or values
     * @return true if intersection is empty
     */
    public static <T> boolean isIntersectionEmpty(List<T> first, List<T> second, Comparator<T> comparator) {
        int firstIndex = 0;
        int secondIndex = 0;
        while (firstIndex < first.size() && secondIndex < second.size()) {
            int cmp = comparator.compare(first.get(firstIndex), second.get(secondIndex));
            if (cmp == 0) {
                assert first.get(firstIndex).equals(second.get(secondIndex));
                return false;
            } else if (cmp < 0) {
                firstIndex++;
            } else {
                secondIndex++;
            }
        }
        return true;
    }
}
