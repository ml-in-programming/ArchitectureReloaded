package org.jetbrains.research.groups.ml_methods.algorithm;

import org.apache.log4j.Logger;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.Entity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitySearchResult;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ARI extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(ARI.class);
    private static final double ACCURACY = 1;

    private final List<Entity> units = new ArrayList<>();
    private final List<ClassEntity> classEntities = new ArrayList<>();
    private final AtomicInteger progressCount = new AtomicInteger();
    private ExecutionContext context;

    public ARI() {
        super("ARI", true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        units.clear();
        classEntities.clear();
        final EntitySearchResult entities = context.getEntities();
        classEntities.addAll(entities.getClasses());
        units.addAll(entities.getMethods());
        if (enableFieldRefactorings) {
            units.addAll(entities.getFields());
        }
        progressCount.set(0);
        this.context = context;
        return runParallel(units, context, ArrayList<Refactoring>::new, this::findRefactoring, AlgorithmsUtil::combineLists);
    }

    private List<Refactoring> findRefactoring(Entity entity, List<Refactoring> accumulator) {
        reportProgress((double) progressCount.incrementAndGet() / units.size(), context);
        context.checkCanceled();
        if (!entity.isMovable() || classEntities.size() < 2) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        double difference = Double.POSITIVE_INFINITY;
        ClassEntity targetClass = null;
        for (final ClassEntity classEntity : classEntities) {

            final double distance = entity.distance(classEntity);
            if (distance < minDistance) {
                difference = minDistance - distance;
                minDistance = distance;
                targetClass = classEntity;
            } else if (distance - minDistance < difference) {
                difference = distance - minDistance;
            }
        }

        if (targetClass == null) {
            LOGGER.warn("targetClass is null for " + entity.getName());
            return accumulator;
        }
        final String targetClassName = targetClass.getName();
        if (!targetClassName.equals(entity.getClassName())) {
            accumulator.add(Refactoring.createRefactoring(entity.getName(), targetClassName,
                    AlgorithmsUtil.getGapBasedAccuracyRating(minDistance, difference) * ACCURACY,
                    entity.isField(), context.getScope()));
        }
        return accumulator;
    }
}
