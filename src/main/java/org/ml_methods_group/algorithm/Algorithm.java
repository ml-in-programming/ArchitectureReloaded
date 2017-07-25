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

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public abstract class Algorithm {
    private final String name;
    private final boolean isParallelExecution;
    private final int preferredThreadsCount;

    protected Algorithm(String name, boolean isParallelExecution) {
        this.name = name;
        this.isParallelExecution = isParallelExecution;
        preferredThreadsCount = Runtime.getRuntime().availableProcessors();
    }

    public final AlgorithmResult execute(EntitySearchResult entities, ExecutorService service) {
        final long startTime = System.currentTimeMillis();
        final ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
        if (indicator != null) {
            indicator.pushState();
            indicator.setText("Run " + name + "...");
            indicator.setFraction(0);
        }
        final ExecutionContext context =
                new ExecutionContext(isParallelExecution ? requireNonNull(service) : null, indicator, entities);
        final Map<String, String> refactorings;
        try {
            refactorings = calculateRefactorings(context);
        } catch (Exception e) {
            e.printStackTrace();
            return new AlgorithmResult(name, e);
        }
        final long totalTime = System.currentTimeMillis() - startTime;
        if (indicator != null) {
            indicator.popState();
        }
        return new AlgorithmResult(refactorings, name, totalTime, context.usedThreads);
    }

    protected abstract Map<String, String> calculateRefactorings(ExecutionContext context) throws Exception;

    protected void reportProgress(double progress, ExecutionContext context) {
        if (context.indicator != null) {
            context.indicator.setFraction(progress);
        }
    }

    protected final <A, V> A runParallel(List<V> values, ExecutionContext context, Supplier<A> accumulatorFactory,
                                            BiFunction<V, A, A> processor, BinaryOperator<A> combiner) {
        if (context.service == null) {
            throw new UnsupportedOperationException("Parallel execution is disabled");
        }
        final List<Callable<A>> tasks = splitValues(values).stream()
                .sequential()
                .map(list -> new Task<>(list, accumulatorFactory, processor))
                .collect(Collectors.toList());
        context.reportAdditionalThreadsUsed(tasks.size());;
        final List<Future<A>> results = new ArrayList<>();
        for (Callable<A> task : tasks) {
            results.add(context.service.submit(task));
        }
        return results.stream()
                .sequential()
                .map(this::getResult)
                .reduce(combiner)
                .orElseGet(accumulatorFactory);
    }

    private <T> List<List<T>> splitValues(List<T> values) {
        final List<List<T>> lists = new ArrayList<>();
        final int valuesCount = values.size();
        final int blocksCount = Math.min(preferredThreadsCount, values.size());
        final int blockSize = (valuesCount - 1) / blocksCount + 1; // round up

        for (int blockStart = 0; blockStart < valuesCount; blockStart += blockSize) {
            lists.add(values.subList(blockStart, Math.min(blockStart + blockSize, valuesCount)));
        }
        return lists;
    }

    private <T> T getResult(Future<T> future) {
        while(true) {
            try {
                return future.get();
            } catch (InterruptedException ignored) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e); // todo
            }
        }
    }

    protected final class ExecutionContext {
        private final ExecutorService service;
        private final ProgressIndicator indicator;
        public final EntitySearchResult entities;
        private int usedThreads = 1; // default thread

        private ExecutionContext(ExecutorService service, @Nullable ProgressIndicator indicator,
                                 EntitySearchResult entities) {
            this.service = service;
            this.indicator = indicator;
            this.entities = entities;
        }

        private void reportAdditionalThreadsUsed(int count) {
            usedThreads = Math.max(usedThreads, 1 + count);
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
