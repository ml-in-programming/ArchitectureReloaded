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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

public class AlgorithmResult {
    private final Map<String, String> refactorings;
    private final String algorithmName;
    private final long executionTime;
    private final int threadUsed;
    private final Exception exception;

    public AlgorithmResult(@NotNull Map<String, String> refactorings, String algorithmName, long executionTime,
                           int threadUsed) {
        this.refactorings = refactorings;
        this.algorithmName = algorithmName;
        this.executionTime = executionTime;
        this.threadUsed = threadUsed;
        this.exception = null;
    }

    AlgorithmResult(String algorithmName, @NotNull Exception exception) {
        this.refactorings = Collections.emptyMap();
        this.algorithmName = algorithmName;
        this.executionTime = 0;
        this.threadUsed = 0;
        this.exception = exception;
    }

    public Map<String, String> getRefactorings() {
        return Collections.unmodifiableMap(refactorings);
    }

    public String getAlgorithmName() {
        return algorithmName;
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
        return "Results of " + algorithmName + " running" + System.lineSeparator() +
                "  Found " + refactorings.size() + " refactorings" + System.lineSeparator() +
                "  Execution time: " + executionTime + System.lineSeparator() +
                "  Threads used: " + threadUsed;
    }
}
