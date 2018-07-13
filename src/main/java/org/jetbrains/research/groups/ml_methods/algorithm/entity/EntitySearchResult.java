package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * A result that {@link EntitySearcher} produces. This class stores entities that were accepted by
 * an {@link EntitySearcher} as valid.
 */
public class EntitySearchResult {
    private final List<ClassEntity> classes;
    private final List<MethodEntity> methods;
    private final List<FieldEntity> fields;
    private final int propertiesCount;
    private final long searchTime;

    public EntitySearchResult(List<ClassEntity> classes, List<MethodEntity> methods, List<FieldEntity> fields,
                              long searchTime) {
        this.classes = classes;
        this.methods = methods;
        this.fields = fields;
        this.searchTime = searchTime;
        propertiesCount = Stream.of(classes, methods, fields)
                .flatMap(List::stream)
                .map(Entity::getRelevantProperties)
                .mapToInt(RelevantProperties::size)
                .sum();
    }

    /** Returns classes stored in this result. */
    public List<ClassEntity> getClasses() {
        return Collections.unmodifiableList(classes);
    }

    /** Returns methods stored in this result. */
    public List<MethodEntity> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    /** Returns fields stored in this result. */
    public List<FieldEntity> getFields() {
        return Collections.unmodifiableList(fields);
    }

    /** Returns total number of {@link RelevantProperties} over all entities in this result.  */
    public int getPropertiesCount() {
        return propertiesCount;
    }

    public long getSearchTime() {
        return searchTime;
    }
}
