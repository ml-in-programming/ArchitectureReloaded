package org.ml_methods_group.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.ClassOldEntity;
import org.ml_methods_group.algorithm.entity.OldEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.refactoring.Refactoring;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ARI extends OldAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(ARI.class);
    private static final double ACCURACY = 1;

    private final List<OldEntity> units = new ArrayList<>();
    private final List<ClassOldEntity> classEntities = new ArrayList<>();
    private final AtomicInteger progressCount = new AtomicInteger();
    private OldExecutionContext context;

    public ARI() {
        super("ARI", true);
    }

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }

    @Override
    protected List<Refactoring> calculateRefactorings(OldExecutionContext context, boolean enableFieldRefactorings) {
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
        return context.runParallel(units, ArrayList<Refactoring>::new, this::findRefactoring, AlgorithmsUtil::combineLists);
    }

    private List<Refactoring> findRefactoring(OldEntity entity, List<Refactoring> accumulator) {
        context.reportProgress((double) progressCount.incrementAndGet() / units.size());
        context.checkCanceled();
        if (!entity.isMovable() || classEntities.size() < 2) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        double difference = Double.POSITIVE_INFINITY;
        ClassOldEntity targetClass = null;
        for (final ClassOldEntity classEntity : classEntities) {

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
