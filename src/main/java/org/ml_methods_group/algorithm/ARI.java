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

package org.ml_methods_group.algorithm;

import com.sixrr.metrics.MetricCategory;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.MethodEntity;

import java.util.*;
import java.util.concurrent.*;

public class ARI {
    private final List<Entity> units;
    private final List<ClassEntity> classEntities;

    public ARI(Iterable<Entity> entityList) {
        units = new ArrayList<>();
        classEntities = new ArrayList<>();
        for (Entity entity : entityList) {
            if (entity.getCategory() == MetricCategory.Class) {
                classEntities.add((ClassEntity) entity);
            } else {
                units.add(entity);
            }
        }
    }

    public Map<String, String> run() {
        final int preferredThreadsCount = Math.min(Runtime.getRuntime().availableProcessors() + 1,
                units.size());
        ExecutorService executor = Executors.newCachedThreadPool();
        int blockSize = (units.size() - 1) / preferredThreadsCount + 1; // round up
        List<Future<Map<String, String>>> futures = new ArrayList<>();
        final int unitsCount = units.size();
        for (int blockStart = 0; blockStart < unitsCount; blockStart += blockSize) {
            final int blockEnd = Math.min(blockStart + blockSize, unitsCount);
            final Worker worker = new Worker(units.subList(blockStart, blockEnd));
            futures.add(executor.submit(worker));
        }
        final Map<String, String> refactorings = new HashMap<>();
        for (Future<Map<String, String>> future : futures) {
            Map<String, String> currentResult = null;
            do {
                try {
                    currentResult = future.get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    currentResult = Collections.emptyMap();
                }
            } while (currentResult == null);
            refactorings.putAll(currentResult);
        }
        executor.shutdown();
        return refactorings;
    }

    private class Worker implements Callable<Map<String, String>> {
        private final List<Entity> units;

        private Worker(List<Entity> units) {
            this.units = units;
        }

        @Override
        public Map<String, String> call() throws Exception {
            final Map<String, String> refactorings = new HashMap<>();
            for (Entity unit : units) {
                double minDistance = Double.POSITIVE_INFINITY;
                ClassEntity targetClass = null;

                for (final ClassEntity classEntity : classEntities) {
                    if (unit.getCategory() == MetricCategory.Method) {
                        // todo check that its enough
                        if (((MethodEntity) unit).isOverriding()) {
                            continue;
                        }
                    }

                    final double distance = unit.distance(classEntity);
                    if (distance < minDistance) {
                        minDistance = distance;
                        targetClass = classEntity;
                    }
                }

                if (targetClass == null) {
                    System.out.println("!!!!! targetClass is null for " + unit.getName());
                    continue;
                }

                final String targetClassName = targetClass.getName();
                if (!targetClassName.equals(unit.getClassName())) {
                    refactorings.put(unit.getName(), targetClassName);
                    System.out.println("Move " + unit.getName() + " to " + targetClassName);
                }
            }
            return refactorings;
        }
    }
}
