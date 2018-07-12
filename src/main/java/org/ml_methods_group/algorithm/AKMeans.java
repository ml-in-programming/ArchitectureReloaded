package org.ml_methods_group.algorithm;

import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.OldEntity;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;
import org.ml_methods_group.algorithm.refactoring.Refactoring;
import org.ml_methods_group.config.Logging;
import org.ml_methods_group.utils.AlgorithmsUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static org.ml_methods_group.utils.AlgorithmsUtil.getDensityBasedAccuracyRating;

public class AKMeans extends OldAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(AKMeans.class);
    private static final double ACCURACY = 1;

    private final List<OldEntity> points = new ArrayList<>();
    private final List<Integer> indexes = new ArrayList<>();
    private final List<Integer> communityID = new ArrayList<>();
    private final List<Set<OldEntity>> communities = new ArrayList<>();
    private final int steps;
    private int numberOfClasses = 0;

    public AKMeans(int steps) {
        super("AKMeans", true);
        this.steps = steps;
    }

    public AKMeans() {
        this(25);
    }

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }

    private void init(EntitySearchResult entities) {
        LOGGER.info("Init AKMeans");
        points.clear();
        communities.clear();
        communityID.clear();
        numberOfClasses = entities.getClasses().size();
        Stream.of(entities.getMethods(), entities.getFields())
                .flatMap(List::stream)
                .peek(x -> communityID.add(-1))
                .forEach(points::add);
        communityID.addAll(Collections.nCopies(points.size(), -1));
        Stream.iterate(0, x -> x + 1)
                .sequential()
                .limit(points.size())
                .forEach(indexes::add);
    }

    private void initializeCenters() {
        LOGGER.info("Initialize centers");
        final List<OldEntity> entities = new ArrayList<>(points);
        Collections.shuffle(entities);

        for (int i = 0; i < numberOfClasses; i++) {
            final Set<OldEntity> community = new HashSet<>();
            community.add(entities.get(i));
            communityID.set(i, i);
            communities.add(community);
        }
    }

    @Override
    protected List<Refactoring> calculateRefactorings(OldExecutionContext context, boolean enableFieldRefactorings) {
        init(context.getEntities());
        context.checkCanceled();
        initializeCenters();
        context.checkCanceled();

        for (int step = 0; step < steps; step++) {
            LOGGER.info("Start step " + step);
            context.reportProgress((double) step / steps);
            context.checkCanceled();
            final Map<Integer, Integer> movements =
                    context.runParallel(indexes, HashMap::new, this::findNearestCommunity, AlgorithmsUtil::combineMaps);
            for (Entry<Integer, Integer> movement : movements.entrySet()) {
                moveToCommunity(movement.getKey(), movement.getValue());
            }
            LOGGER.info(movements.size() + " movements found");
            if (movements.size() == 0) {
                break;
            }
        }

        final List<Refactoring> refactorings = new ArrayList<>();
        for (Set<OldEntity> community : communities) {
            final Entry<String, Long> dominant = AlgorithmsUtil.getDominantClass(community);
            community.stream()
                    .filter(e -> !e.getClassName().equals(dominant.getKey()))
                    .filter(OldEntity::isMovable)
                    .filter(e -> enableFieldRefactorings || !e.isField())
                    .map(e -> Refactoring.createRefactoring(e.getName(), dominant.getKey(),
                            getDensityBasedAccuracyRating(dominant.getValue(), community.size()) * ACCURACY,
                            e.isField(), context.getScope()))
                    .forEach(refactorings::add);
        }
        return refactorings;
    }

    private Map<Integer, Integer> findNearestCommunity(int entityID, Map<Integer, Integer> accumulator) {
        double minDistance = Double.POSITIVE_INFINITY;
        int targetID = -1;
        final OldEntity entity = points.get(entityID);
        for (int centerID = 0; centerID < communities.size(); centerID++) {
            double distance = distToCommunity(entity, centerID);
            if (distance < minDistance) {
                minDistance = distance;
                targetID = centerID;
            }
        }
        if (targetID != -1 && targetID != communityID.get(entityID)) {
            accumulator.put(entityID, targetID);
        }
        return accumulator;
    }

    private double distToCommunity(OldEntity entity, int centerID) {
        final Set<OldEntity> community = communities.get(centerID);
        if (community.isEmpty()) {
            return Double.POSITIVE_INFINITY;
        }
        double maxDistance = 0.0;
        for (OldEntity point : community) {
            final double distance = entity.distance(point);
            maxDistance = Math.max(distance, maxDistance);
            if (maxDistance == Double.POSITIVE_INFINITY) {
                break;
            }
        }

        return maxDistance;
    }

    private void moveToCommunity(int entityID, int centerID) {
        final OldEntity entity = points.get(entityID);
        final int currentCommunity = communityID.get(entityID);
        if (currentCommunity != -1) {
            communities.get(currentCommunity).remove(points.get(entityID));
        }
        communities.get(centerID).add(entity);
        communityID.set(entityID, centerID);
    }
}
