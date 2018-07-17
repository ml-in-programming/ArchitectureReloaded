package org.jetbrains.research.groups.ml_methods.algorithm;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.List;

public class AlgorithmsRepository {
    private static final List<Algorithm> ALGORITHMS = Arrays.asList(
            new ARI(),
            new AKMeans(),
            new CCDA(),
            new HAC(),
            new MRI()
    );


    @Contract(pure = true)
    public static List<Algorithm> getAvailableAlgorithms() {
        return ALGORITHMS;
    }
}
