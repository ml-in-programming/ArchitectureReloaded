package org.jetbrains.research.groups.ml_methods.algorithm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.AlgorithmsRepository.AlgorithmType;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;

import java.util.Collections;
import java.util.List;

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
        this.refactorings = Collections.emptyList();
        this.algorithmType = algorithmType;
        this.executionTime = 0;
        this.threadUsed = 0;
        this.exception = exception;
    }

    public List<CalculatedRefactoring> getRefactorings() {
        return Collections.unmodifiableList(refactorings);
    }

    public AlgorithmType getAlgorithmType() {
        return algorithmType;
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
        return "Results of " + algorithmType + " running" + System.lineSeparator() +
                "  Found " + refactorings.size() + " refactorings" + System.lineSeparator() +
                "  Execution time: " + executionTime + System.lineSeparator() +
                "  Threads used: " + threadUsed;
    }
}
