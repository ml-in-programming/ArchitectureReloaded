package org.jetbrains.research.groups.ml_methods.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.OldEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitySearchResult;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.RelevantProperties;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CCDA extends OldAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(CCDA.class);
    private static final double ACCURACY = 1;

    private final Map<String, Integer> communityIds = new HashMap<>();
    private final Map<OldEntity, Integer> entityCommunities = new HashMap<>();
    private final List<String> idCommunity = new ArrayList<>();
    private final List<Integer> aCoefficients = new ArrayList<>();
    private final List<OldEntity> nodes = new ArrayList<>();
    private final Map<String, Set<String>> graph = new HashMap<>();
    private OldExecutionContext context;

    private double quality;
    private double edges;
    private static final double eps = 5e-4;

    public CCDA() {
        super("CCDA", true);

    }

    private void init() {
        final EntitySearchResult entities = context.getEntities();
        LOGGER.info("Init CCDA");
        communityIds.clear();
        entityCommunities.clear();
        idCommunity.clear();
        nodes.clear();
        aCoefficients.clear();
        quality = 0.0;
        entities.getClasses().stream()
                .peek(entity -> communityIds.put(entity.getName(), communityIds.size() + 1))
                .peek(entity -> entityCommunities.put(entity, communityIds.get(entity.getClassName())))
                .map(OldEntity::getName)
                .forEach(idCommunity::add);
        Stream.of(entities.getFields(), entities.getMethods())
                .flatMap(List::stream)
                .filter(entity -> communityIds.containsKey(entity.getClassName()))
                .peek(entity -> communityIds.put(entity.getName(), communityIds.get(entity.getClassName())))
                .peek(entity -> entityCommunities.put(entity, communityIds.get(entity.getClassName())))
                .forEach(nodes::add);
        aCoefficients.addAll(Collections.nCopies(idCommunity.size() + 1, 0));
        buildGraph();
    }

    private void buildGraph() {
        LOGGER.info("Building graph");
        graph.clear();
        int iteration = 0;
        for (OldEntity entity : nodes) {
            final RelevantProperties properties = entity.getRelevantProperties();
            final Set<String> neighbors = graph.getOrDefault(entity.getName(), new HashSet<>());

            properties.getMethods()
                    .forEach(name -> addNode(name, entity, neighbors));

            for (String field : properties.getFields()) {
                addNode(field, entity, neighbors);
            }

            context.checkCanceled();
            graph.put(entity.getName(), neighbors);
            iteration++;
            context.reportProgress((0.1 * iteration) / nodes.size());
        }
    }

    private void addNode(String entityName, OldEntity entity, Collection<String> neighbors) {
        if (entityName.equals(entity.getName()) || !communityIds.containsKey(entityName)) {
            return;
        }
        neighbors.add(entityName);
        graph.computeIfAbsent(entityName, k -> new HashSet())
                .add(entity.getName());
    }

    @Override
    protected List<Refactoring> calculateRefactorings(OldExecutionContext context, boolean enableFieldRefactorings) {
        this.context = context;
        init();
        final Map<OldEntity, String> refactorings = new HashMap<>();
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
            communityIds.put(optimum.targetEntity.getName(), optimum.community);
            entityCommunities.put(optimum.targetEntity, optimum.community);
            progress = Math.max(progress, eps / optimum.delta);
            context.reportProgress(0.1 + 0.9 * progress);
            LOGGER.info("Finish iteration. Current quality is " + quality + " (delta is " + optimum.delta + ")");
            context.checkCanceled();
        }

        final Map<Integer, List<OldEntity>> entities = entityCommunities.entrySet().stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().stream().map(Map.Entry::getKey).collect(Collectors.toList())));

        final Map<Integer, Map.Entry<String, Long>> dominants = entities.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> AlgorithmsUtil.getDominantClassOld(e.getValue()))
                );

        return refactorings.entrySet().stream()
                .map(entry -> {
                    int id = communityIds.get(entry.getValue());
                    long dominant = dominants.get(id).getValue();
                    long size = entities.get(id).size();
                    if (enableFieldRefactorings || !entry.getKey().isField()) {
                        return Refactoring.createRefactoring(entry.getKey().getName(), entry.getValue(),
                                AlgorithmsUtil.getDensityBasedAccuracyRating(dominant, size) * ACCURACY,
                                entry.getKey().isField(), context.getScope());
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Holder attempt(OldEntity entity, Holder optimum) {
        final int currentCommunityID = communityIds.get(entity.getName());
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

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }

    private class Holder {
        private double delta = 0;
        private OldEntity targetEntity;
        private int community = -1;
    }

    private Holder max(Holder first, Holder second) {
        return first.delta >= second.delta ? first : second;
    }

    private double move(OldEntity ent, int to, boolean rollback) {
        final String name = ent.getName();
        final int from = communityIds.get(name);
        double dq = 0.0;
        dq += Math.pow(aCoefficients.get(from) * 1.0 / edges, 2.0);
        dq += Math.pow(aCoefficients.get(to) * 1.0 / edges, 2.0);

        int aFrom = 0;
        int aTo = 0;
        int de = 0;

        for (String neighbor : graph.get(name)) {
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
        for (String node : graph.keySet()) {
            edges += (double) graph.get(node).size();
        }
        edges /= 2.0;

        for (int i = 1; i <= idCommunity.size(); ++i) {
            final String community = idCommunity.get(i - 1);
            int e = 0;
            int a = 0;

            for (String node : graph.keySet()) {
                if (!communityIds.containsKey(node)) {
                    LOGGER.warn("ERROR: unknown community: " + node);
                }

                if (!communityIds.get(node).equals(communityIds.get(community))) {
                    continue;
                }
                for (String neighbor : graph.get(node)) {
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
