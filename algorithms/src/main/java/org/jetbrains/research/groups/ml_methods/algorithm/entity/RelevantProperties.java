package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

/**
 * A set of properties of an {@link CodeEntity}. A property is a method, a class or a field which has
 * some relation to the {@link CodeEntity} that stores object of this class. Each property has a
 * weight which corresponds to importance of this property.
 */
public class RelevantProperties {
    private final @NotNull Map<MethodEntity, Integer> notOverrideMethods = new HashMap<>();

    private final @NotNull Map<ClassEntity, Integer> classes = new HashMap<>();

    private final @NotNull Map<FieldEntity, Integer> fields = new HashMap<>();

    private final @NotNull Map<MethodEntity, Integer> overrideMethods = new HashMap<>();

    private final Integer DEFAULT_PROPERTY_WEIGHT = 1;

    void addNotOverrideMethod(final @NotNull MethodEntity method) {
        addNotOverrideMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }


    void addNotOverrideMethod(final @NotNull MethodEntity method, final @NotNull Integer weight) {
        if (notOverrideMethods.getOrDefault(method, 0) < weight) {
            notOverrideMethods.put(method, weight);
        }
    }

    void addClass(final @NotNull ClassEntity aClass) {
        addClass(aClass, DEFAULT_PROPERTY_WEIGHT);
    }

    void addClass(final @NotNull ClassEntity aClass, final @NotNull Integer weight) {
        if (classes.getOrDefault(aClass, 0) < weight) {
            classes.put(aClass, weight);
        }
    }

    void addField(final @NotNull FieldEntity field) {
        addField(field, DEFAULT_PROPERTY_WEIGHT);
    }

    void addField(final @NotNull FieldEntity field, final @NotNull Integer weight) {
        if (fields.getOrDefault(field, 0) < weight) {
            fields.put(field, weight);
        }
    }

    void addOverrideMethod(final @NotNull MethodEntity method) {
        addOverrideMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    void addOverrideMethod(final @NotNull MethodEntity method, final @NotNull Integer weight) {
        if (overrideMethods.getOrDefault(method, 0) < weight) {
            overrideMethods.put(method, weight);
        }
    }

    int numberOfMethods() {
        return notOverrideMethods.size();
    }

    public Set<FieldEntity> getFields() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Set<MethodEntity> getNotOverrideMethods() {
        return Collections.unmodifiableSet(notOverrideMethods.keySet());
    }
    
    public Set<MethodEntity> getOverrideMethods() {
        return Collections.unmodifiableSet(overrideMethods.keySet());
    }

    public Set<ClassEntity> getClasses() {
        return Collections.unmodifiableSet(classes.keySet());
    }

    public int size() {
        return getWeightedSize(classes) + getWeightedSize(fields) + getWeightedSize(notOverrideMethods);
    }

    public int getWeight(final @NotNull CodeEntity entity) {
        return classes.getOrDefault(entity, 0)
                + notOverrideMethods.getOrDefault(entity, 0)
                + fields.getOrDefault(entity, 0);
    }

    private int getWeightedSize(Map<?, Integer> m) {
        return m.values().stream().mapToInt(Integer::valueOf).sum();
    }

    public int sizeOfIntersection(final @NotNull RelevantProperties properties) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::min;
        result += sizeOfIntersectWeighted(classes, properties.classes, bop);
        result += sizeOfIntersectWeighted(notOverrideMethods, properties.notOverrideMethods, bop);
        result += sizeOfIntersectWeighted(overrideMethods, properties.overrideMethods, bop);
        result += sizeOfIntersectWeighted(fields, properties.fields, bop);

        return result;
    }

    private static int sizeOfIntersectWeighted(Map<?, Integer> m1, Map<?, Integer> m2, BinaryOperator<Integer> f) {
        return m1.entrySet().stream()
                .filter(e -> m2.containsKey(e.getKey()))
                .mapToInt(e -> f.apply(e.getValue(), m2.get(e.getKey())))
                .sum();
    }

    public int sizeOfUnion(final @NotNull RelevantProperties other) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::max;
        result += size() + other.size();
        result -= sizeOfIntersectWeighted(classes, other.classes, bop);
        result -= sizeOfIntersectWeighted(notOverrideMethods, other.notOverrideMethods, bop);
        result -= sizeOfIntersectWeighted(overrideMethods, other.overrideMethods, bop);
        result -= sizeOfIntersectWeighted(fields, other.fields, bop);
        return result;
    }

    public RelevantProperties copy() {
        final RelevantProperties copy = new RelevantProperties();
        copy.classes.putAll(classes);
        copy.overrideMethods.putAll(overrideMethods);
        copy.notOverrideMethods.putAll(notOverrideMethods);
        copy.fields.putAll(fields);
        return copy;
    }
}