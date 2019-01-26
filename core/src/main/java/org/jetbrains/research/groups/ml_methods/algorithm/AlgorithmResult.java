package org.jetbrains.research.groups.ml_methods.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;

import java.util.Collections;
import java.util.List;

public class AlgorithmResult {
    private final List<CalculatedRefactoring> refactorings;
    private final Algorithm algorithm;
    private final long executionTime;
    private final int threadUsed;
    private final Exception exception;

    AlgorithmResult(
        final @NotNull List<CalculatedRefactoring> refactorings,
        final @NotNull Algorithm algorithm,
        final long executionTime,
        final int threadUsed
    ) {
        this.refactorings = refactorings;
        this.algorithm = algorithm;
        this.executionTime = executionTime;
        this.threadUsed = threadUsed;
        this.exception = null;
    }

    AlgorithmResult(final @NotNull Algorithm algorithm, final @NotNull Exception exception) {
        this.refactorings = Collections.emptyList();
        this.algorithm = algorithm;
        this.executionTime = 0;
        this.threadUsed = 0;
        this.exception = exception;
    }

    public List<CalculatedRefactoring> getRefactorings() {
        return Collections.unmodifiableList(refactorings);
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public int getThreadUsed() {
        return threadUsed;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    public boolean isSuccess() {
        return exception == null;
    }

    public String getReport() {
        return "Results of " + algorithm.getName() + " running" + System.lineSeparator() +
                "  Found " + refactorings.size() + " refactorings" + System.lineSeparator() +
                "  Execution time: " + executionTime + System.lineSeparator() +
                "  Threads used: " + threadUsed;
    }
}
