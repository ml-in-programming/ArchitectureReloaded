package org.jetbrains.research.groups.ml_methods.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AlgorithmResult {
    private final List<CalculatedRefactoring> refactorings;
    private final AlgorithmType algorithmType;
    private final long executionTime;
    private final int threadUsed;
    private final Exception exception;

    AlgorithmResult(@NotNull List<CalculatedRefactoring> refactorings, AlgorithmType algorithmType, long executionTime,
                    int threadUsed) {
        this.refactorings = refactorings;
        this.algorithmType = algorithmType;
        this.executionTime = executionTime;
        this.threadUsed = threadUsed;
        this.exception = null;
    }

    AlgorithmResult(AlgorithmType algorithmType, @NotNull Exception exception) {
        this.refactorings = null;
        this.algorithmType = algorithmType;
        this.executionTime = 0;
        this.threadUsed = 0;
        this.exception = exception;
    }

    public List<CalculatedRefactoring> getRefactorings() {
        throwIfNotSuccess();
        return Collections.unmodifiableList(Objects.requireNonNull(refactorings));
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
    }

    public long getExecutionTime() {
        throwIfNotSuccess();
        return executionTime;
    }

    public int getThreadUsed() {
        throwIfNotSuccess();
        return threadUsed;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    boolean isSuccess() {
        return exception == null;
    }

    private void throwIfNotSuccess() {
        if (!isSuccess()) {
            throw new AlgorithmFailedException("Algorithm ended with exception. Requested data cannot be retrieved.");
        }
    }

    String getReport() {
        return isSuccess() ? "Results of " + algorithmType + " running" + System.lineSeparator() +
                "  Found " + Objects.requireNonNull(refactorings).size() + " refactorings" + System.lineSeparator() +
                "  Execution time: " + executionTime + System.lineSeparator() +
                "  Threads used: " + threadUsed :
                algorithmType + " failed with exception: " + Objects.requireNonNull(exception).getMessage();
    }
}
