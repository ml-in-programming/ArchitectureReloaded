/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

import com.sixrr.metrics.Metric;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.algorithm.entity.EntitySearchResult;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface Algorithm {
    @NotNull AlgorithmResult oldExecute(
        @NotNull EntitySearchResult entities,
        @Nullable ExecutorService service,
        boolean enableFieldRefactorings
    );

    /**
     * Returns a textual description of this algorithm.
     */
    @NotNull String getDescription();

    /**
     * Returns an array of metrics from which a features vectors for this particular algorithm
     * should be constructed.
     * Important contract is that this method must always return equal lists, i.e. lists that
     * contain same elements in the same order.
     */
    @NotNull List<Class<? extends Metric>> requiredMetrics();
}
