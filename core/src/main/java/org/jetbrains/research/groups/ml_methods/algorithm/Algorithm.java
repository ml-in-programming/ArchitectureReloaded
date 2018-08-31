package org.jetbrains.research.groups.ml_methods.algorithm;

import com.sixrr.metrics.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.AttributesStorage;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;

/**
 * An algorithm that analyses given {@link AttributesStorage} and produces refactoring suggestions
 * as an {@link AlgorithmResult}.
 * This class does not represent an algorithm state of execution but represent algorithm itself as
 * idea. {@link Algorithm#execute} method should create actual execution state for each run if
 * needed.
 */
public interface Algorithm {
    /**
     * Executes this algorithm on an input.
     *
     * @param attributes input which consists of different attributes derived from code entities.
     * @param service {@link ExecutorService} in case of parallel computations.
     * @param enableFieldRefactorings {@code true} if there is a request to search for
     *                                            "move field" refactoring.
     * @return result of algorithm execution which contains suggested refactorings.
     */
    @NotNull AlgorithmResult execute(
        @NotNull AttributesStorage attributes,
        @Nullable ExecutorService service,
        boolean enableFieldRefactorings
    );

    /**
     * Returns a short textual description of this algorithm.
     */
    @NotNull String getDescriptionString();

    /**
     * Returns a type of algorithm. Should be used to identify algorithm and request it by enum type not by name.
     */
    @NotNull AlgorithmType getAlgorithmType();

    /**
     * Returns an array of metrics from which a features vectors for this particular algorithm
     * should be constructed.
     * Important contract is that this method must always return equal lists, i.e. lists that
     * contain same metrics in the same order.
     */
    @NotNull List<Metric> requiredMetrics();
}
