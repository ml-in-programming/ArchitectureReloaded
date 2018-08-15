package org.jetbrains.research.groups.ml_methods.algorithm;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AlgorithmsRepository {
    private static final List<Algorithm> ALGORITHMS = Arrays.asList(
            new ARI(),
            new CCDA(),
            new HAC()
    );

    public static Optional<Algorithm> getAlgorithmByName(String algorithmName) {
        return ALGORITHMS.stream().filter(algorithm -> algorithm.getDescriptionString().equals(algorithmName)).findAny();
    }

    public enum AlgorithmType {
        ARI, CCDA, HAC
    }

    @Contract(pure = true)
    public static List<Algorithm> getAvailableAlgorithms() {
        return ALGORITHMS;
    }
}
