package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * This storage contains all code entities (classes, methods, fields...) that have passed
 * {@link EntitySearcher} filtering, i.e. considered to be relevant and useful for algorithms. Each
 * entity also stores its {@link RelevantProperties}.
 */
public class EntitiesStorage {
    private final @NotNull List<ClassEntity> classes;

    private final @NotNull List<MethodEntity> methods;

    private final @NotNull List<FieldEntity> fields;

    private final long buildTime;

    /**
     * Creates storage from actual entities.
     */
    public EntitiesStorage(
        final @NotNull List<ClassEntity> classes,
        final @NotNull List<MethodEntity> methods,
        final @NotNull List<FieldEntity> fields,
        final long buildTime
    ) {
        this.classes = classes;
        this.methods = methods;
        this.fields = fields;

        this.buildTime = buildTime;
    }

    public @NotNull List<ClassEntity> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    public @NotNull List<MethodEntity> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    public @NotNull List<FieldEntity> getFields() {
        return Collections.unmodifiableList(fields);
    }

    public long getBuildTime() {
        return buildTime;
    }

    /** Returns sum of sizes of {@link RelevantProperties} over all entities in this storage.  */
    public int getPropertiesCount() {
        return Stream.of(classes, methods, fields)
                .flatMap(List::stream)
                .map(CodeEntity::getRelevantProperties)
                .mapToInt(RelevantProperties::size)
                .sum();
    }
}
