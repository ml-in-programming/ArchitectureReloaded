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

import java.util.Collections;
import java.util.Map;

public class AlgorithmResult {
    private final Map<String, String> refactorings;
    private final String algorithmName;
    private final long executionTime;
    private final int threadUsed;

    public AlgorithmResult(Map<String, String> refactorings, String algorithmName, long executionTime, int threadUsed) {
        this.refactorings = refactorings;
        this.algorithmName = algorithmName;
        this.executionTime = executionTime;
        this.threadUsed = threadUsed;
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
}
