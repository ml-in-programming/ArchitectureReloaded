package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.psi.PsiClass;
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
                ClassAttributes targetClassAttributes = null;
                for (final ClassAttributes classAttributes : classAttributes) {

                    final double distance = distanceCalculator.distance(entity, classAttributes);
                    if (distance < minDistance) {
                        difference = minDistance - distance;
                        minDistance = distance;
                        targetClassAttributes = classAttributes;
                    } else if (distance - minDistance < difference) {
                        difference = distance - minDistance;
                    }
                }

                if (targetClassAttributes == null) {
                    LOGGER.warn("targetClass is null for " + entity.getOriginalEntity().getIdentifier());
                    return accumulator;
                }
                final String targetClassName = targetClassAttributes.getOriginalEntity().getIdentifier();
                if (!targetClassName.equals(entity.getOriginalEntity().getContainingClassName())) {
                    double accuracy = AlgorithmsUtil.getGapBasedAccuracyRating(minDistance, difference) * ACCURACY;
                    PsiClass targetClass = targetClassAttributes.getOriginalClass().getPsiClass();

                    accumulator.add(entity.accept(new ElementAttributesVisitor<Refactoring>() {
                        @Override
                        public Refactoring visit(final @NotNull ClassAttributes classAttributes) {
                            throw new IllegalArgumentException("Entity is a class");
                        }

                        @Override
                        public Refactoring visit(final @NotNull MethodAttributes methodAttributes) {
                            return new MoveMethodRefactoring(
                                methodAttributes.getOriginalMethod().getPsiMethod(),
                                targetClass,
                                accuracy
                            );
                        }

                        @Override
                        public Refactoring visit(final @NotNull FieldAttributes fieldAttributes) {
                            return new MoveFieldRefactoring(
                                fieldAttributes.getOriginalField().getPsiField(),
                                targetClass,
                                accuracy
                            );
                        }
                    }));
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
