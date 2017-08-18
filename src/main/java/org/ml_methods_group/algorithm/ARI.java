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

import org.apache.log4j.Logger;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.config.Logging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ARI extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(ARI.class);

    private final List<Entity> units = new ArrayList<>();
    private final List<ClassEntity> classEntities = new ArrayList<>();
    private final AtomicInteger progressCount = new AtomicInteger();
    private ExecutionContext context;

    public ARI() {
        super("ARI", true);
    }

    @Override
    protected Map<String, String> calculateRefactorings(ExecutionContext context) {
        units.clear();
        classEntities.clear();
        classEntities.addAll(context.entities.getClasses());
        units.addAll(context.entities.getMethods());
        units.addAll(context.entities.getFields());
        progressCount.set(0);
        this.context = context;
        return runParallel(units, context, HashMap<String, String>::new, this::findRefactoring, Algorithm::combineMaps);
    }

    private Map<String, String> findRefactoring(Entity entity, Map<String, String> accumulator) {
        reportProgress((double) progressCount.incrementAndGet() / units.size(), context);
        context.checkCanceled();
        if (!entity.isMovable()) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        ClassEntity targetClass = null;
        for (final ClassEntity classEntity : classEntities) {
            final double distance = entity.distance(classEntity);
            if (distance < minDistance) {
                minDistance = distance;
                targetClass = classEntity;
            }
        }

        if (targetClass == null) {
            LOGGER.warn("targetClass is null for " + entity.getName());
            return accumulator;
        }
        final String targetClassName = targetClass.getName();
        if (!targetClassName.equals(entity.getClassName())) {
            accumulator.put(entity.getName(), targetClassName);
        }
        return accumulator;
    }
}
