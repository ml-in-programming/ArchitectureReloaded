package org.jetbrains.research.groups.ml_methods.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.*;
import org.jetbrains.research.groups.ml_methods.algorithm.distance.DistanceCalculator;
import org.jetbrains.research.groups.ml_methods.algorithm.distance.RelevanceBasedDistanceCalculator;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveFieldRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ARI extends AbstractAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(ARI.class);
    private static final double ACCURACY = 1;

    private static final @NotNull DistanceCalculator distanceCalculator = RelevanceBasedDistanceCalculator.getInstance();

    public ARI() {
        super("ARI", true);
    }

    @Override
    protected @NotNull Executor setUpExecutor() {
        return new Executor() {
            private final List<ElementAttributes> units = new ArrayList<>();
            private final List<ClassAttributes> classAttributes = new ArrayList<>();
            private final AtomicInteger progressCount = new AtomicInteger();
            private ExecutionContext context;

            @NotNull
            @Override
            public List<Refactoring> execute(@NotNull ExecutionContext context, boolean enableFieldRefactorings) throws Exception {
                units.clear();
                classAttributes.clear();
                final AttributesStorage attributes = context.getAttributesStorage();
                classAttributes.addAll(attributes.getClassesAttributes());
                units.addAll(attributes.getMethodsAttributes());
                if (enableFieldRefactorings) {
                    units.addAll(attributes.getFieldsAttributes());
                }
                progressCount.set(0);
                this.context = context;
                return context.runParallel(units, ArrayList<Refactoring>::new, this::findRefactoring, AlgorithmsUtil::combineLists);
            }

            private List<Refactoring> findRefactoring(ElementAttributes entity, List<Refactoring> accumulator) {
                context.reportProgress((double) progressCount.incrementAndGet() / units.size());
                context.checkCanceled();
                if (!entity.getOriginalEntity().isMovable() || classAttributes.size() < 2) {
                    return accumulator;
                }
                double minDistance = Double.POSITIVE_INFINITY;
                double difference = Double.POSITIVE_INFINITY;
                ClassAttributes targetClass = null;
                for (final ClassAttributes classAttributes : classAttributes) {

                    final double distance = distanceCalculator.distance(entity, classAttributes);
                    if (distance < minDistance) {
                        difference = minDistance - distance;
                        minDistance = distance;
                        targetClass = classAttributes;
                    } else if (distance - minDistance < difference) {
                        difference = distance - minDistance;
                    }
                }

                if (targetClass == null) {
                    LOGGER.warn("targetClass is null for " + entity.getOriginalEntity().getIdentifier());
                    return accumulator;
                }
                final String targetClassName = targetClass.getOriginalEntity().getIdentifier();
                if (!targetClassName.equals(entity.getOriginalEntity().getContainingClassName())) {
                    double accuracy = AlgorithmsUtil.getGapBasedAccuracyRating(minDistance, difference) * ACCURACY;

                    if (entity instanceof MethodAttributes) {
                        MethodAttributes methodAttributes = (MethodAttributes) entity;

                        accumulator.add(
                            new MoveMethodRefactoring(
                                methodAttributes.getOriginalMethod().getPsiMethod(),
                                targetClass.getOriginalClass().getPsiClass(),
                                accuracy
                            )
                        );
                    } else if (entity instanceof FieldAttributes) {
                        FieldAttributes fieldAttributes = (FieldAttributes) entity;

                        accumulator.add(
                            new MoveFieldRefactoring(
                                fieldAttributes.getOriginalField().getPsiField(),
                                targetClass.getOriginalClass().getPsiClass(),
                                accuracy
                            )
                        );
                    } else {
                        throw new IllegalArgumentException("Entity is not a method or a field");
                    }
                }
                return accumulator;
            }
        };
    }

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }
}
