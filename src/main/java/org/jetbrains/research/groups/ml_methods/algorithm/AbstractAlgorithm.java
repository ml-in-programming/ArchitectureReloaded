package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;
import org.jetbrains.research.groups.ml_methods.algorithm.attributes.AttributesStorage;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Partial implementation of {@link Algorithm} interface. Main purpose of this class is to
 * implement {@link Algorithm#execute} method so that it handles things that has small relation to
 * actual algorithm.
 * Also this class introduces {@link Executor} which represents a state for one actual
 * algorithm run. {@link Executor} accepts convenient {@link ExecutionContext} which it
 * can use to interact with environment.
 */
public abstract class AbstractAlgorithm implements Algorithm {
    private static final Logger LOGGER = Logging.getLogger(AbstractAlgorithm.class);

    private final AlgorithmType algorithmType;

    private final boolean enableParallelExecution;

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    public AbstractAlgorithm(AlgorithmType algorithmType, boolean enableParallelExecution) {
        this.algorithmType = algorithmType;
        this.enableParallelExecution = enableParallelExecution;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String getDescriptionString() {
        return algorithmType.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull AlgorithmResult execute(
        final @NotNull AttributesStorage attributes,
        final @Nullable ExecutorService service,
        final boolean enableFieldRefactorings
    ) {
        LOGGER.info(algorithmType + " started");
        final long startTime = System.currentTimeMillis();
        final ProgressIndicator indicator;

        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }

        indicator.pushState();
        indicator.setText("Running " + algorithmType + "...");
        indicator.setFraction(0);

        final ExecutionContext context = new ExecutionContext(
            enableParallelExecution ? requireNonNull(service) : null,
            indicator,
            attributes
        );

        final List<CalculatedRefactoring> refactorings;
        try {
            refactorings = setUpExecutor().execute(context, enableFieldRefactorings);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(algorithmType + " finished with error: " + e);
            return new AlgorithmResult(algorithmType, e);
        }

        final long time = System.currentTimeMillis() - startTime;
        indicator.popState();

        final AlgorithmResult result = new AlgorithmResult(refactorings, algorithmType, time, context.usedThreads);

        LOGGER.info(algorithmType + " successfully finished");
        LOGGER.info(result.getReport());

        return result;
    }

    /**
     * Returns {@link Executor} for a new algorithm run. May create a new one or reuse old. It's
     * up to algorithm implementation.
     */
    protected abstract @NotNull Executor setUpExecutor();

    /**
     * A state which will be used for one algorithm run: receive input, process it and get suggested
     * refactorings as output.
     */
    public interface Executor {
        /**
         * Executes algorithm on one input to get one output.
         *
         * @param context a context for this execution.
         * @param enableFieldRefactorings {@code true} if user also looks for "move field"
         *                                refactorings.
         * @return suggested refactorings.
         * @throws Exception if any kind of error occurs during algorithm execution.
         */
        @NotNull List<CalculatedRefactoring> execute(
            @NotNull ExecutionContext context,
            boolean enableFieldRefactorings
        ) throws Exception;
    }

    /**
     * A context for {@link AbstractAlgorithm} execution. It stores input and provide different
     * services as parallel execution, progress report, etc.
     */
    protected final class ExecutionContext {
        private final ExecutorService service;

        private final ProgressIndicator indicator;

        private final AttributesStorage attributes;

        private final int preferredThreadsCount;

        private int usedThreads = 1; // default thread

        private ExecutionContext(
            final ExecutorService service,
            final ProgressIndicator indicator,
            final @NotNull AttributesStorage attributes
        ) {
            this.service = service;
            this.indicator = indicator;
            this.attributes = attributes;

            preferredThreadsCount = Runtime.getRuntime().availableProcessors();
        }

        /**
         * Returns {@link AttributesStorage} with attributes for all code entities.
         */
        public @NotNull AttributesStorage getAttributesStorage() {
            return attributes;
        }

        public void checkCanceled() {
            indicator.checkCanceled();
        }

        public void reportProgress(double progress) {
            indicator.setFraction(progress);
        }

        public final <A, V> A runParallel(
            List<V> values,
            Supplier<A> accumulatorFactory,
            BiFunction<V, A, A> processor,
            BinaryOperator<A> combiner
        ) {
            if (service == null) {
                throw new UnsupportedOperationException("Parallel execution is disabled");
            }

            final List<Callable<A>> tasks = splitValues(values).stream()
                    .sequential()
                    .map(list -> new Task<>(list, accumulatorFactory, processor))
                    .collect(Collectors.toList());

            reportAdditionalThreadsUsed(tasks.size());
            final List<Future<A>> results = new ArrayList<>();
            for (Callable<A> task : tasks) {
                results.add(service.submit(task));
            }
            return results.stream()
                    .sequential()
                    .map(this::getResult)
                    .reduce(combiner)
                    .orElseGet(accumulatorFactory);
        }

        private void reportAdditionalThreadsUsed(int count) {
            usedThreads = Math.max(usedThreads, 1 + count);
        }

        private <T> List<List<T>> splitValues(List<T> values) {
            final List<List<T>> lists = new ArrayList<>();
            if (values.size() != 0) {
                final int valuesCount = values.size();
                final int blocksCount = Math.min(preferredThreadsCount, values.size());
                final int blockSize = (valuesCount - 1) / blocksCount + 1; // round up

                for (int blockStart = 0; blockStart < valuesCount; blockStart += blockSize) {
                    lists.add(values.subList(blockStart, Math.min(blockStart + blockSize, valuesCount)));
                }
            }
            return lists;
        }

        private <T> T getResult(Future<T> future) {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException ignored) {
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof ProcessCanceledException) {
                        throw (ProcessCanceledException) e.getCause();
                    } else {
                        throw new RuntimeException(e); // todo
                    }
                }
            }
        }

        private class Task<A, V> implements Callable<A> {
            private final List<V> values;
            private final Supplier<A> accumulatorFactory;
            private final BiFunction<V, A, A> processor;

            private Task(List<V> values, Supplier<A> accumulatorFactory, BiFunction<V, A, A> processor) {
                this.values = values;
                this.accumulatorFactory = accumulatorFactory;
                this.processor = processor;
            }

            public A call() {
                A accumulator = accumulatorFactory.get();
                for (V value : values) {
                    accumulator = processor.apply(value, accumulator);
                }
                return accumulator;
            }
        }
    }
}
