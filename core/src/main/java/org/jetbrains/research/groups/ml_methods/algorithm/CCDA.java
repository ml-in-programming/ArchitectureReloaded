package org.jetbrains.research.groups.ml_methods.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.AttributesStorage;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ClassAttributes;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.ClassInnerEntityAttributes;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.*;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveFieldRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;
import org.jetbrains.research.groups.ml_methods.logging.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CCDA extends AbstractAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(CCDA.class);
    private static final double ACCURACY = 1;

    public CCDA() {
        super("CCDA", true);

    }

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }

    @Override
    protected @NotNull AbstractAlgorithm.Executor setUpExecutor() {
        return new Executor();
    }

    private static class Executor implements AbstractAlgorithm.Executor {
        private final Map<CodeEntity, Integer> communityIds = new HashMap<>();
        private final Map<CodeEntity, Integer> entityCommunities = new HashMap<>();
        private final List<ClassEntity> idCommunity = new ArrayList<>();
        private final List<Integer> aCoefficients = new ArrayList<>();
        private final List<ClassInnerEntity> nodes = new ArrayList<>();
        private final Map<ClassInnerEntity, Set<ClassInnerEntity>> graph = new HashMap<>();
        private ExecutionContext context;

        private double quality;
        private double edges;
        private static final double eps = 5e-4;

        @Override
        public @NotNull List<CalculatedRefactoring> execute(
            final @NotNull ExecutionContext context,
            final boolean enableFieldRefactorings
        ) throws Exception {
            this.context = context;
            init();
            final Map<ClassInnerEntity, ClassEntity> refactorings = new HashMap<>();
            context.checkCanceled();
            quality = calculateQualityIndex();
            double progress = 0;
            while (true) {
                final Holder optimum = context.runParallel(nodes, Holder::new, this::attempt, this::max);
                if (optimum.delta <= eps) {
                    break;
                }
                refactorings.put(optimum.targetEntity, idCommunity.get(optimum.community - 1));
                move(optimum.targetEntity, optimum.community, false);
                communityIds.put(optimum.targetEntity, optimum.community);
                entityCommunities.put(optimum.targetEntity, optimum.community);
                progress = Math.max(progress, eps / optimum.delta);
                context.reportProgress(0.1 + 0.9 * progress);
                LOGGER.info("Finish iteration. Current quality is " + quality + " (delta is " + optimum.delta + ")");
                context.checkCanceled();
            }

            final Map<Integer, List<CodeEntity>> entities = entityCommunities.entrySet().stream()
                    .collect(Collectors.groupingBy(Map.Entry::getValue))
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList())));

            final Map<Integer, Map.Entry<ClassEntity, Long>> dominants = entities.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> AlgorithmsUtil.getDominantClass(e.getValue()))
                    );

            return refactorings.entrySet().stream()
                    .map(entry -> {
                        int id = communityIds.get(entry.getValue());
                        long dominant = dominants.get(id).getValue();
                        long size = entities.get(id).size();

                        CodeEntity entity = entry.getKey();
                        ClassEntity target = entry.getValue();

                        MoveToClassRefactoring refactoring = entity.accept(new CodeEntityVisitor<MoveToClassRefactoring>() {
                            @Override
                            public MoveToClassRefactoring visit(@NotNull ClassEntity classEntity) {
                                throw new IllegalStateException("Unexpected class entity");
                            }

                            @Override
                            public MoveToClassRefactoring visit(@NotNull MethodEntity methodEntity) {
                                return new MoveMethodRefactoring(methodEntity.getPsiMethod(), target.getPsiClass());
                            }

                            @Override
                            public MoveToClassRefactoring visit(@NotNull FieldEntity fieldEntity) {
                                if (!enableFieldRefactorings) {
                                    return null;
                                }

                                return new MoveFieldRefactoring(fieldEntity.getPsiField(), target.getPsiClass());
                            }
                        });

                        if (refactoring == null) {
                            return null;
                        }

                        return new CalculatedRefactoring(refactoring, AlgorithmsUtil.getDensityBasedAccuracyRating(dominant, size) * ACCURACY);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        private void init() {
            final AttributesStorage entities = context.getAttributesStorage();
            LOGGER.info("Init CCDA");
            communityIds.clear();
            entityCommunities.clear();
            idCommunity.clear();
            nodes.clear();
            aCoefficients.clear();
            quality = 0.0;
            entities.getClassesAttributes().stream().map(ClassAttributes::getOriginalClass)
                    .peek(entity -> communityIds.put(entity, communityIds.size() + 1))
                    .peek(entity -> entityCommunities.put(entity, communityIds.get(entity)))
                    .forEach(idCommunity::add);
            Stream.of(entities.getFieldsAttributes(), entities.getMethodsAttributes())
                    .flatMap(List::stream).map(ClassInnerEntityAttributes::getClassInnerEntity)
                    .filter(entity -> communityIds.containsKey(entity.getContainingClass()))
                    .peek(entity -> communityIds.put(entity, communityIds.get(entity.getContainingClass())))
                    .peek(entity -> entityCommunities.put(entity, communityIds.get(entity.getContainingClass())))
                    .forEach(nodes::add);
            aCoefficients.addAll(Collections.nCopies(idCommunity.size() + 1, 0));
            buildGraph();
        }

        private void buildGraph() {
            LOGGER.info("Building graph");
            graph.clear();
            int iteration = 0;
            for (ClassInnerEntity entity : nodes) {
                final RelevantProperties properties = entity.getRelevantProperties();
                final Set<ClassInnerEntity> neighbors = graph.getOrDefault(entity, new HashSet<>());

                properties.getNotOverrideMethods()
                        .forEach(method -> addNode(method, entity, neighbors));

                for (FieldEntity field : properties.getFields()) {
                    addNode(field, entity, neighbors);
                }

                context.checkCanceled();
                graph.put(entity, neighbors);
                iteration++;
                context.reportProgress((0.1 * iteration) / nodes.size());
            }
        }

        private void addNode(ClassInnerEntity entityName, ClassInnerEntity entity, Collection<ClassInnerEntity> neighbors) {
            if (entityName.equals(entity) || !communityIds.containsKey(entityName)) {
                return;
            }
            neighbors.add(entityName);
            graph.computeIfAbsent(entityName, k -> new HashSet<>())
                    .add(entity);
        }

        private Holder attempt(ClassInnerEntity entity, Holder optimum) {
            final int currentCommunityID = communityIds.get(entity);
            for (int i = 1; i <= idCommunity.size(); ++i) {
                if (i == currentCommunityID) {
                    continue;
                }
                final double delta = move(entity, i, true);
                if (delta >= optimum.delta) {
                    optimum.delta = delta;
                    optimum.targetEntity = entity;
                    optimum.community = i;
                }
            }
            context.checkCanceled();
            return optimum;
        }

        private class Holder {
            private double delta = 0;
            private ClassInnerEntity targetEntity;
            private int community = -1;
        }

        private Holder max(Holder first, Holder second) {
            return first.delta >= second.delta ? first : second;
        }

        private double move(ClassInnerEntity ent, int to, boolean rollback) {
            final int from = communityIds.get(ent);
            double dq = 0.0;
            dq += Math.pow(aCoefficients.get(from) * 1.0 / edges, 2.0);
            dq += Math.pow(aCoefficients.get(to) * 1.0 / edges, 2.0);

            int aFrom = 0;
            int aTo = 0;
            int de = 0;

            for (ClassInnerEntity neighbor : graph.get(ent)) {
                if (communityIds.get(neighbor) == from) {
                    de--;
                } else {
                    aFrom++;
                }

                if (communityIds.get(neighbor) == to) {
                    de++;
                } else {
                    aTo++;
                }
            }

            aFrom = aCoefficients.get(from) - aFrom;
            aTo = aCoefficients.get(to) + aTo;

            if (!rollback) {
                aCoefficients.set(from, aFrom);
                aCoefficients.set(to, aTo);
            }

            dq += (double) de * 1.0 / edges;
            dq -= Math.pow((double) aFrom * 1.0 / edges, 2.0);
            dq -= Math.pow((double) aTo * 1.0 / edges, 2.0);

            if (!rollback) {
                quality += dq;
            }

            return dq;
        }

        private double calculateQualityIndex() {
            double qualityIndex = 0.0;

            edges = 0.0;
            for (ClassInnerEntity node : graph.keySet()) {
                edges += (double) graph.get(node).size();
            }
            edges /= 2.0;

            for (int i = 1; i <= idCommunity.size(); ++i) {
                final ClassEntity community = idCommunity.get(i - 1);
                int e = 0;
                int a = 0;

                for (ClassInnerEntity node : graph.keySet()) {
                    if (!communityIds.containsKey(node)) {
                        LOGGER.warn("ERROR: unknown community: " + node);
                    }

                    if (!communityIds.get(node).equals(communityIds.get(community))) {
                        continue;
                    }
                    for (ClassInnerEntity neighbor : graph.get(node)) {
                        if (communityIds.get(neighbor).equals(communityIds.get(community))) {
                            e++;
                        } else {
                            a++;
                        }
                    }
                }

                e /= 2;
                a += e;
                qualityIndex += ((double) e * 1.0 / edges) - Math.pow((double) a * 1.0 / edges, 2.0);
                aCoefficients.set(i, a);
            }

            return qualityIndex;
        }
    }
}
