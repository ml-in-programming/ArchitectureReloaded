package org.jetbrains.research.groups.ml_methods.algorithm.attributes;

import com.sixrr.metrics.Metric;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.*;

import java.util.*;

/**
 * One object of this class must be created per {@link Algorithm} execution. This class stores all
 * relevant code entities (classes, methods, ...) along with their
 * {@link RelevantProperties} and features extracted
 * specially for one particular {@link Algorithm} from metrics this algorithm requested through
 * {@link Algorithm#requiredMetrics}.
 */
public class AttributesStorage {
    private final @NotNull List<ClassAttributes> classesAttributes;

    private final @NotNull List<MethodAttributes> methodsAttributes;

    private final @NotNull List<FieldAttributes> fieldsAttributes;

    /**
     * Creates storage for attributes.
     *
     * @param entities entities attributes will be constructed from.
     * @param metrics metrics that must be used to create features vectors.
     * @param metricsRun result of all metrics calculations.
     * @throws NoRequestedMetricException if there are some missing metric calculation results.
     */
    public AttributesStorage(
        final @NotNull EntitiesStorage entities,
        final @NotNull List<Metric> metrics,
        final @NotNull MetricsRun metricsRun
    ) throws NoRequestedMetricException {
        Map<ClassEntity, ClassAttributes> attributesOfClass = new HashMap<>();

        classesAttributes = new ArrayList<>();
        for (ClassEntity classEntity : entities.getClasses()) {
            ClassAttributes classAttributes = new ClassAttributes(
                classEntity,
                extractFeatures(classEntity, metrics, metricsRun)
            );

            classesAttributes.add(classAttributes);
            attributesOfClass.put(classEntity, classAttributes);
        }

        methodsAttributes = new ArrayList<>();
        for (MethodEntity methodEntity : entities.getMethods()) {
            methodsAttributes.add(
                new MethodAttributes(
                    methodEntity,
                    extractFeatures(methodEntity, metrics, metricsRun),
                    attributesOfClass.get(methodEntity.getContainingClass())
                )
            );
        }

        fieldsAttributes = new ArrayList<>();
        for (FieldEntity fieldEntity : entities.getFields()) {
            fieldsAttributes.add(new FieldAttributes(
                fieldEntity,
                new double[0],
                attributesOfClass.get(fieldEntity.getContainingClass())
            ));
        }
    }

    public @NotNull List<ClassAttributes> getClassesAttributes() {
        return Collections.unmodifiableList(classesAttributes);
    }

    public @NotNull List<MethodAttributes> getMethodsAttributes() {
        return Collections.unmodifiableList(methodsAttributes);
    }
    public @NotNull List<FieldAttributes> getFieldsAttributes() {
        return Collections.unmodifiableList(fieldsAttributes);
    }

    private @NotNull double[] extractFeatures(
        final @NotNull CodeEntity entity,
        final @NotNull List<Metric> metrics,
        final @NotNull MetricsRun metricsRun
    ) throws NoRequestedMetricException {
        MetricsResult result = metricsRun.getResultsForCategory(entity.getMetricCategory());

        final int dimension =
            (int) metrics.stream()
                .filter(it -> it.getCategory().equals(entity.getMetricCategory()))
                .count();

        double[] features = new double[dimension];

        int pointer = 0;
        for (Metric metric : metrics) {
            if (metric.getCategory().equals(entity.getMetricCategory())) {
                Double metricValue = result.getValueForMetric(metric, entity.getIdentifier());
                if (metricValue != null) {
                    features[pointer++] = metricValue;
                } else {
                    throw new NoRequestedMetricException(
                        "Requested metric '" +
                        metric.getID() +
                        "' is not computed for object '" +
                        entity.getIdentifier() +
                        "'"
                    );
                }
            }
        }

        return features;
    }
}
