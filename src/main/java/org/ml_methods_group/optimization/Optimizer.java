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

package org.ml_methods_group.optimization;

import org.jenetics.*;
import org.jenetics.engine.Engine;
import org.jenetics.engine.codecs;
import org.jenetics.util.IntRange;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.algorithm.entity.PropertiesStrategy;
import org.ml_methods_group.utils.RefactoringBase;
import org.ml_methods_group.utils.RefactoringBase.Status;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.jenetics.engine.limit.bySteadyFitness;
import static org.jenetics.engine.EvolutionResult.toBestPhenotype;
import static org.ml_methods_group.algorithm.entity.PropertiesStrategy.*;

public class Optimizer {
    private final RefactoringBase refactoringBase;
    private final Function<PropertiesStrategy, Map<String, String>> runner;

    public Optimizer(RefactoringBase refactoringBase, Function<PropertiesStrategy, Map<String, String>> runner) {
        this.refactoringBase = refactoringBase;
        this.runner = runner;
    }

    public PropertiesStrategy runOptimization() {
        IntRange[] ranges = new IntRange[20];
        Arrays.setAll(ranges, i -> IntRange.of(0, 1));
        ranges[19] = IntRange.of(0, 3);
        Engine<IntegerGene, Integer> engine = Engine.builder(
                this::fitness,
                codecs.ofVector(ranges))
                .populationSize(100)
                .optimize(Optimize.MAXIMUM)
                .build();
        final Phenotype<IntegerGene, Integer> best = engine.stream()
                .limit(100)
                .peek(e -> System.out.println(e.getGeneration() + " finished | res =" + e.getBestFitness()))
                .collect(toBestPhenotype());
        System.out.println("best is " + best.getFitness() + " gens: " + best.getGenotype());
        int[] values = best.getGenotype().stream()
                .sequential()
                .flatMap(Chromosome::stream)
                .mapToInt(IntegerGene::getAllele)
                .toArray();
        System.out.println(Arrays.toString(values));
        return new PropertiesStrategy(values);
    }

    private int fitness(int[] values) {
        final Map<String, String> refactorings = runner.apply(new PropertiesStrategy(values));
        return getValue(refactorings);
    }

    private int getValue(Map<String, String> refactorings) {
        int result = 0;
        for (Map.Entry<String, String> refactoring : refactorings.entrySet()) {
            result += getValue(refactoringBase.getStatusFor(refactoring.getKey(), refactoring.getValue()));
        }
        return result;
    }

    private int getValue(Status status) {
        switch (status) {
            case VERY_BAD:
                return -10;
            case BAD:
                return -5;
            case NEUTRAL:
                return 0;
            case UNKNOWN:
                return -1;
            case GOOD:
                return 4;
            case VERY_GOOD:
                return 8;
        }
        return 0;
    }
}
