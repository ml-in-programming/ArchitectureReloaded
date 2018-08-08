package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.psi.PsiClass;
import com.sixrr.metrics.Metric;
import com.sixrr.stockmetrics.classMetrics.NumAttributesAddedMetric;
import com.sixrr.stockmetrics.classMetrics.NumMethodsClassMetric;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.*;
import org.jetbrains.research.groups.ml_methods.algorithm.distance.DistanceCalculator;
import org.jetbrains.research.groups.ml_methods.algorithm.distance.RelevanceBasedDistanceCalculator;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveFieldRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil.getDensityBasedAccuracyRating;

public class HAC extends AbstractAlgorithm {
    private static final Logger LOGGER = Logging.getLogger(HAC.class);
    private static final double ACCURACY = 1;

    private static final @NotNull DistanceCalculator distanceCalculator = RelevanceBasedDistanceCalculator.getInstance();

    public HAC() {
        super("HAC", true);
    }

    @Override
    public @NotNull List<Metric> requiredMetrics() {
        return Arrays.asList(new NumMethodsClassMetric(), new NumAttributesAddedMetric());
    }

    @Override
    protected @NotNull Executor setUpExecutor() {
        return new HACExecutor();
    }

    private static class HACExecutor implements Executor {
        private final SortedSet<Triple> heap = new TreeSet<>();
        private final Map<Long, Triple> triples = new HashMap<>();
        private final Set<Community> communities = new HashSet<>();
        private final AtomicInteger progressCounter = new AtomicInteger();
        private ExecutionContext context;
        private int idGenerator = 0;

        @Override
        public @NotNull List<CalculatedRefactoring> execute(
                final @NotNull ExecutionContext context,
                final boolean enableFieldRefactorings
        ) throws Exception {
            init(context);
            final int initialCommunitiesCount = communities.size();
            while (!heap.isEmpty()) {
                final Triple minTriple = heap.first();
                invalidateTriple(minTriple);
                final Community first = minTriple.first;
                final Community second = minTriple.second;
                mergeCommunities(first, second);
                context.reportProgress(1 - 0.1 * communities.size() / initialCommunitiesCount);
                context.checkCanceled();
            }

            final List<CalculatedRefactoring> refactorings = new ArrayList<>();
            for (Community community : communities) {
                final int entitiesCount = community.entities.size();
                if (entitiesCount == 0) {
                    continue;
                }
                final Entry<ClassAttributes, Long> dominantClass = AlgorithmsUtil.getDominantClassForAttributes(community.entities);
                final ClassAttributes classAttributes = dominantClass.getKey();

                LOGGER.info("Generate class name for community (id = " + community.id +"): " + classAttributes.getOriginalClass().getIdentifier());
                for (ElementAttributes entity : community.entities) {
                    if (!(entity instanceof ClassInnerEntityAttributes)) {
                        continue;
                    }

                    ClassInnerEntityAttributes attributes = (ClassInnerEntityAttributes) entity;

                    if (!attributes.getContainingClassAttributes().equals(classAttributes)) {
                        double accuracy = getDensityBasedAccuracyRating(dominantClass.getValue(), entitiesCount) * ACCURACY;
                        PsiClass targetClass = classAttributes.getOriginalClass().getPsiClass();

                        attributes.accept(new ElementAttributesVisitor<Void>() {
                            @Override
                            public Void visit(@NotNull ClassAttributes classAttributes) {
                                throw new IllegalStateException("Unexpected ClassAttributes");
                            }

                            @Override
                            public Void visit(@NotNull MethodAttributes methodAttributes) {
                                refactorings.add(new CalculatedRefactoring(
                                    new MoveMethodRefactoring(
                                        methodAttributes.getOriginalMethod().getPsiMethod(),
                                        targetClass
                                    ),
                                    accuracy
                                ));

                                return null;
                            }

                            @Override
                            public Void visit(@NotNull FieldAttributes fieldAttributes) {
                                if (enableFieldRefactorings) {
                                    refactorings.add(new CalculatedRefactoring(
                                        new MoveFieldRefactoring(
                                            fieldAttributes.getOriginalField().getPsiField(),
                                            targetClass
                                        ),
                                        accuracy
                                    ));
                                }

                                return null;
                            }
                        });
                    }
                }
            }
            Triple.clearPool();
            return refactorings;
        }

        private void init(ExecutionContext context) {
            LOGGER.info("Init HAC");
            this.context = context;
            heap.clear();
            communities.clear();
            idGenerator = 0;
            progressCounter.set(0);
            final AttributesStorage entities = context.getAttributesStorage();
            Stream.of(entities.getMethodsAttributes(), entities.getFieldsAttributes())
                    .flatMap(List::stream)
                    .map(this::singletonCommunity)
                    .forEach(communities::add);
            final List<Community> communitiesAsList = new ArrayList<>(communities);
            Collections.shuffle(communitiesAsList);
            final List<Triple> toInsert =
                    context.runParallel(communitiesAsList, ArrayList::new, this::findTriples, AlgorithmsUtil::combineLists);
            toInsert.forEach(this::insertTriple);
            LOGGER.info("Built heap (" + heap.size() + " triples)");
        }

        private List<Triple> findTriples(Community community, List<Triple> accumulator) {
            final ElementAttributes representative = community.entities.get(0);
            for (Community another : communities) {
                if (another == community) {
                    break;
                }
                final double distance = distanceCalculator.distance(representative, another.entities.get(0));
                if (distance < 1) {
                    accumulator.add(new Triple(distance, community, another));
                }
            }
            context.reportProgress(0.9 * (double) progressCounter.incrementAndGet() / communities.size());
            context.checkCanceled();
            return accumulator;
        }

        private Community mergeCommunities(Community first, Community second) {
            final List<ElementAttributes> merged;
            if (first.entities.size() < second.entities.size()) {
                merged = second.entities;
                merged.addAll(first.entities);
            } else {
                merged = first.entities;
                merged.addAll(second.entities);
            }

            final Community newCommunity = new Community(merged);
            communities.remove(first);
            communities.remove(second);

            for (Community community : communities) {
                final long fromFirstID = getTripleID(first, community);
                final long fromSecondID = getTripleID(second, community);
                final Triple fromFirst = triples.get(fromFirstID);
                final Triple fromSecond = triples.get(fromSecondID);
                final double newDistance = Math.max(getDistance(fromFirst), getDistance(fromSecond));
                invalidateTriple(fromFirst);
                invalidateTriple(fromSecond);
                insertTripleIfNecessary(newDistance, newCommunity, community);
            }
            communities.add(newCommunity);
            return newCommunity;
        }

        private double getDistance(@Nullable Triple triple) {
            return triple == null? Double.POSITIVE_INFINITY : triple.distance;
        }

        private long getTripleID(Community first, Community second) {
            if (second.id > first.id) {
                return getTripleID(second, first);
            }
            return first.id * 1_000_000_009L + second.id;
        }

        private void insertTriple(@NotNull Triple triple) {
            triples.put(getTripleID(triple.first, triple.second), triple);
            heap.add(triple);
        }

        private void insertTripleIfNecessary(double distance, Community first, Community second) {
            if (distance > 1.0) {
                return;
            }
            final Triple triple = Triple.createTriple(distance, first, second);
            insertTriple(triple);
        }

        private void invalidateTriple(@Nullable Triple triple) {
            if (triple == null) {
                return;
            }
            final long tripleID = getTripleID(triple.first, triple.second);
            triples.remove(tripleID);
            heap.remove(triple);
            triple.release();
        }

        private Community singletonCommunity(ElementAttributes element) {
            final List<ElementAttributes> singletonList = new ArrayList<>(1);
            singletonList.add(element);
            return new Community(singletonList);
        }

        private class Community implements Comparable<Community> {
            private final List<ElementAttributes> entities;
            private final int id;

            Community(List<ElementAttributes> entities) {
                this.entities = entities;
                id = idGenerator++;
            }

            @Override
            public int compareTo(@NotNull Community o) {
                return id - o.id;
            }

            @Override
            public int hashCode() {
                return id;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Community && ((Community) obj).id == id;
            }
        }

        private static class Triple implements Comparable<Triple> {
            private static final Queue<Triple> triplesPoll = new ArrayDeque<>();

            private double distance;
            private Community first;
            private Community second;

            Triple(double distance, Community first, Community second) {
                this.distance = distance;
                this.first = first;
                this.second = second;
            }

            static Triple createTriple(double distance, Community first, Community second) {
                if (triplesPoll.isEmpty()) {
                    return new Triple(distance, first, second);
                }
                final Triple triple = triplesPoll.poll();
                triple.distance = distance;
                triple.first = first;
                triple.second = second;
                return triple;
            }

            static void clearPool() {
                triplesPoll.clear();
            }

            void release() {
                triplesPoll.add(this);
            }

            @Override
            public int compareTo(@NotNull Triple other) {
                if (other == this) {
                    return 0;
                }
                if (distance != other.distance) {
                    return Double.compare(distance, other.distance);
                }
                if (first != other.first) {
                    return first.compareTo(other.first);
                }
                return second.compareTo(other.second);
            }
        }
    }
}
