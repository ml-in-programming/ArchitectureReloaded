package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.sixrr.metrics.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.attributes.AttributesStorage;

import java.util.List;
import java.util.concurrent.ExecutorService;

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
     * @param scope this argument is needed only for backward compatibility with old version of
     *              {@link org.ml_methods_group.algorithm.refactoring.Refactoring} class. If
     *              {@link org.ml_methods_group.algorithm.refactoring.Refactoring#createRefactoring}
     *              method is completely removed then this argument should also be removed.
     * @return result of algorithm execution which contains suggested refactorings.
     */
    @NotNull AlgorithmResult execute(
        @NotNull AttributesStorage attributes,
        @Nullable ExecutorService service,
        boolean enableFieldRefactorings,
        @NotNull AnalysisScope scope
    );

    /**
     * Returns a short textual description of this algorithm.
     */
    @NotNull String getDescriptionString();

    /**
     * Returns an array of metrics from which a features vectors for this particular algorithm
     * should be constructed.
     * Important contract is that this method must always return equal lists, i.e. lists that
     * contain same metrics in the same order.
     */
    @NotNull List<Metric> requiredMetrics();
}
