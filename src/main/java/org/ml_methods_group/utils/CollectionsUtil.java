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

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class CollectionsUtil {

    public static <T> int fastSizeOfIntersect(Set<T> first, Set<T> second) {
        if (first.size() < second.size()) {
            return sizeOfIntersect(first, second);
        }
        return sizeOfIntersect(second, first);
    }

    public static <T> int sizeOfIntersect(Collection<T> first, Collection<T> second) {
        int counter = 0;
        for (T item : first) {
            if (second.contains(item)) {
                counter++;
            }
        }
        return counter;
    }

    public static <T> Set<T> union(Set<T> first, Set<T> second) {
        return new Union<>(first, second);
    }

    private static class Union<T> extends AbstractSet<T> {

        private final Set<T> smallSet;
        private final Set<T> bigSet;

        private Union(Set<T> first, Set<T> second) {
            smallSet = first.size() < second.size()? first : second;
            bigSet = smallSet == first? second : first;
        }

        @NotNull
        @Override
        public Iterator<T> iterator() {
            return new UnionIterator();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            return bigSet.contains(o) || smallSet.contains(o);
        }

        private class UnionIterator implements Iterator<T> {
            private T nextElement;
            private Iterator<T> first = bigSet.iterator();
            private Iterator<T> second = smallSet.iterator();

            @Override
            public boolean hasNext() {
                tryFindNextElement();
                return nextElement == null;
            }

            @Override
            public T next() {
                tryFindNextElement();
                final T result = nextElement;
                nextElement = null;
                return result;
            }

            private void tryFindNextElement() {
                if (nextElement != null) {
                    return;
                }
                if (first.hasNext()) {
                    nextElement = first.next();
                    return;
                }
                while (second.hasNext()) {
                    final T element = second.next();
                    if (!bigSet.contains(element)) {
                        nextElement = element;
                        return;
                    }
                }
            }
        }
    }
}
