package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.psi.PsiElement;
import com.sixrr.metrics.Metric;
import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ElementAttributes;
import org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil;

import java.util.*;

/**
 * Code entity like class, method or field. Entity can be used inside suggestion algorithms.
 * It has a vector of features and {@link RelevantProperties}.
 *
 * This is an old class which has too many responsibility. {@link CodeEntity} and
 * {@link ElementAttributes} should be used instead.
 */
@Deprecated
public abstract class OldEntity {
    private static final VectorCalculator CLASS_ENTITY_CALCULATOR = new VectorCalculator()
            .addMetricDependence(NumMethodsClassMetric.class)
            .addMetricDependence(NumAttributesAddedMetric.class);

    private static final VectorCalculator METHOD_ENTITY_CALCULATOR = new VectorCalculator()
            .addConstValue(0)
            .addConstValue(0);

    private static final VectorCalculator FIELD_ENTITY_CALCULATOR = new VectorCalculator()
            .addConstValue(0)
            .addConstValue(0);

    private static final int DIMENSION = CLASS_ENTITY_CALCULATOR.getDimension();

    static {
        assert CLASS_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert METHOD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
        assert FIELD_ENTITY_CALCULATOR.getDimension() == DIMENSION;
    }

    private final RelevantProperties relevantProperties;
    private final String name;
    private PsiElement element;
    private double[] vector;
    protected boolean isMovable = true;

    /** Initializes this class with a given {@link PsiElement}. */
    public OldEntity(PsiElement element) {
        this.name = ApplicationManager.getApplication()
                .runReadAction((Computable<String>) () -> PsiSearchUtil.getHumanReadableName(element));
        this.element = element;
        relevantProperties = new RelevantProperties();
    }

    public PsiElement getPsiElement() {
        return element;
    }

    protected OldEntity(OldEntity original) {
        relevantProperties = original.relevantProperties.copy();
        name = original.name;
        element = original.element;
        vector = Arrays.copyOf(original.vector, original.vector.length);
        isMovable = original.isMovable;
    }

    void calculateVector(MetricsRun metricsRun) {
        vector = getCalculatorForEntity().calculateVector(metricsRun, this);
    }

    private double square(double value) {
        return value * value;
    }

    public double distance(OldEntity entity) {
        double ans = 0.0;
        double w = 0.0;

        if (this.getCategory().equals(entity.getCategory())) {
            for (int i = 0; i < vector.length; i++) {
                w += square(vector[i] + entity.vector[i]);
            }
        } else {
            for (double aVector : vector) {
                w += square(aVector);
            }

            for (double aVector : entity.vector) {
                w += square(aVector);
            }
        }

        ans += w == 0 ? 0 : 1.0 / (w + 1);
        final int rpIntersect = entity.relevantProperties.sizeOfIntersection(relevantProperties);
        if (rpIntersect == 0) {
            return Double.POSITIVE_INFINITY;
        }
        ans += (1 - rpIntersect /
                (1.0 * relevantProperties.sizeOfUnion(entity.getRelevantProperties())));

        return Math.sqrt(ans);
    }

    static void normalize(Iterable<? extends OldEntity> entities) {
        for (int i = 0; i < DIMENSION; i++) {
            double mx = 0.0;
            for (OldEntity entity : entities) {
                mx = Math.max(mx, entity.vector[i]);
            }

            if (mx == 0.0) {
                continue;
            }

            for (OldEntity entity : entities) {
                entity.vector[i] /= mx;
            }
        }
    }

    public RelevantProperties getRelevantProperties() {
        return relevantProperties;
    }

    public String getName() {
        return name;
    }

    private VectorCalculator getCalculatorForEntity() {
        if (getClass() == ClassOldEntity.class) {
            return CLASS_ENTITY_CALCULATOR;
        } else if (getClass() == MethodOldEntity.class) {
            return METHOD_ENTITY_CALCULATOR;
        } else if (getClass() == FieldOldEntity.class) {
            return FIELD_ENTITY_CALCULATOR;
        }
        throw new UnsupportedOperationException("Such type of entity isn't supported: " + getClass());
    }

    public static Set<Class<? extends Metric>> getRequestedMetrics() {
        final Set<Class<? extends Metric>> result = new HashSet<>();
        result.addAll(CLASS_ENTITY_CALCULATOR.getRequestedMetrics());
        result.addAll(METHOD_ENTITY_CALCULATOR.getRequestedMetrics());
        result.addAll(FIELD_ENTITY_CALCULATOR.getRequestedMetrics());
        return result;
    }

    public boolean isMovable() {
        return isMovable;
    }

    abstract public MetricCategory getCategory();

    abstract public String getClassName();

    abstract public OldEntity copy();

    abstract public boolean isField();
}