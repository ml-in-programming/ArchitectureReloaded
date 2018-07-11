/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.algorithm;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.attributes.AttributesStorage;
import org.ml_methods_group.config.Logging;

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

public abstract class AbstractAlgorithm implements Algorithm {
    private static final Logger LOGGER = Logging.getLogger(AbstractAlgorithm.class);

    private final String name;

    private final boolean enableParallelExecution;

    public AbstractAlgorithm(String name, boolean enableParallelExecution) {
        this.name = name;
        this.enableParallelExecution = enableParallelExecution;
    }

    @Override
    public @NotNull String getDescription() {
        return name;
    }

    @Override
    public @NotNull AlgorithmResult execute(
        @NotNull AttributesStorage attributes,
        @Nullable ExecutorService service,
        boolean enableFieldRefactorings
    ) {
        LOGGER.info(name + " started");
        final long startTime = System.currentTimeMillis();
        final ProgressIndicator indicator;

        if (ProgressManager.getInstance().hasProgressIndicator()) {
            indicator = ProgressManager.getInstance().getProgressIndicator();
        } else {
            indicator = new EmptyProgressIndicator();
        }

        indicator.pushState();
        indicator.setText("Running " + name + "...");
        indicator.setFraction(0);

        final AlgorithmExecutionContext context = new AlgorithmExecutionContext(
            enableParallelExecution ? requireNonNull(service) : null,
            indicator,
            attributes
        );

        final List<Refactoring> refactorings;
        try {
            refactorings = setUpExecutor().execute(context, enableFieldRefactorings);
        } catch (ProcessCanceledException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(name + " finished with error: " + e);
            return new AlgorithmResult(name, e);
        }

        final long time = System.currentTimeMillis() - startTime;
        indicator.popState();

        final AlgorithmResult result = new AlgorithmResult(refactorings, name, time, context.usedThreads);

        LOGGER.info(name + " successfully finished");
        LOGGER.info(result.getReport());

        return result;
    }

    protected abstract @NotNull AlgorithmExecutor setUpExecutor();

    public interface AlgorithmExecutor {
        @NotNull List<Refactoring> execute(
            @NotNull AlgorithmExecutionContext context,
            boolean enableFieldRefactorings
        ) throws Exception;
    }

    protected final class AlgorithmExecutionContext {
        private final ExecutorService service;

        private final ProgressIndicator indicator;

        private final AttributesStorage attributes;

        private final int preferredThreadsCount;

        private int usedThreads = 1; // default thread

        private AlgorithmExecutionContext(
            final ExecutorService service,
            final ProgressIndicator indicator,
            final @NotNull AttributesStorage attributes
        ) {
            this.service = service;
            this.indicator = indicator;
            this.attributes = attributes;

            preferredThreadsCount = Runtime.getRuntime().availableProcessors();
        }

        public @NotNull AttributesStorage getAttributes() {
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
