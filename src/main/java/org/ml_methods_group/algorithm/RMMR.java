package org.ml_methods_group.algorithm;

import org.apache.log4j.Logger;
import org.ml_methods_group.algorithm.entity.ClassEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.entity.MethodEntity;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RMMR extends Algorithm {
    public static final String NAME = "RMMR";

    private static final Logger LOGGER = Logging.getLogger(RMMR.class);

    private final Map<ClassEntity, Set<MethodEntity>> methodsByClass = new HashMap<>();
    private final List<MethodEntity> units = new ArrayList<>();
    private final List<ClassEntity> classEntities = new ArrayList<>();
    private final AtomicInteger progressCount = new AtomicInteger();
    private ExecutionContext context;

    public RMMR() {
        super(NAME, true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) throws Exception {
        if (enableFieldRefactorings) {
            // TODO: write to LOGGER or throw Exception? Change UI: disable field checkbox if only RMMR is chosen.
            LOGGER.error("Field refactorings are not supported",
                    new UnsupportedOperationException("Field refactorings are not supported"));
        }
        this.context = context;
        init();
        return runParallel(units, context, ArrayList::new, this::findRefactoring, AlgorithmsUtil::combineLists);
    }

    private void init() {
        final EntitySearchResult entities = context.getEntities();
        LOGGER.info("Init RMMR");
        units.clear();
        classEntities.clear();

        classEntities.addAll(entities.getClasses());
        units.addAll(entities.getMethods());
        progressCount.set(0);

        entities.getMethods().forEach(methodEntity -> {
            List<ClassEntity> methodClassEntity = entities.getClasses().stream()
                    .filter(classEntity -> methodEntity.getClassName().equals(classEntity.getName()))
                    .collect(Collectors.toList());
            if (methodClassEntity.size() != 1) {
                LOGGER.error("Found more than 1 class that has this method");
            }
            methodsByClass.computeIfAbsent(methodClassEntity.get(0), anyKey -> new HashSet<>()).add(methodEntity);
        });
    }

    private List<Refactoring> findRefactoring(MethodEntity entity, List<Refactoring> accumulator) {
        reportProgress((double) progressCount.incrementAndGet() / units.size(), context);
        context.checkCanceled();
        if (!entity.isMovable() || classEntities.size() < 2) {
            return accumulator;
        }
        double minDistance = Double.POSITIVE_INFINITY;
        double difference = Double.POSITIVE_INFINITY;
        ClassEntity targetClass = null;
        for (final ClassEntity classEntity : classEntities) {
            final double distance = getDistance(entity, classEntity);
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
        double accuracy = (1 - minDistance) * difference;  // TODO: Maybe consider amount of entities?
        if (!targetClassName.equals(entity.getClassName())) {
            accumulator.add(new Refactoring(entity.getName(), targetClassName, accuracy, entity.isField()));
        }
        return accumulator;
    }

    private double getDistance(MethodEntity methodEntity, ClassEntity classEntity) {
        int number = 0;
        double sumOfDistances = 0;

        if (methodsByClass.containsKey(classEntity)) {
            for (MethodEntity methodEntityInClass : methodsByClass.get(classEntity)) {
                if (!methodEntity.equals(methodEntityInClass)) {
                    sumOfDistances += getDistance(methodEntity, methodEntityInClass);
                    number++;
                }
            }
        }

        return number == 0 ? 1 : sumOfDistances / number;
    }

    private double getDistance(MethodEntity methodEntity1, MethodEntity methodEntity2) {
        // TODO: Maybe add to methodEntity2 source class where it is located?
        Set<String> method1Classes = methodEntity1.getRelevantProperties().getClasses();
        Set<String> method2Classes = methodEntity2.getRelevantProperties().getClasses();
        int sizeOfIntersection = intersection(method1Classes, method2Classes).size();
        int sizeOfUnion = union(method1Classes, method2Classes).size();
        return (sizeOfIntersection == 0) ? 1 : 1 - (double) sizeOfIntersection / sizeOfUnion;
    }

    private <T> Set<T> intersection(Set<T> set1, Set<T> set2) {
        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }

    private <T> Set<T> union(Set<T> set1, Set<T> set2) {
        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);
        return union;
    }
}
