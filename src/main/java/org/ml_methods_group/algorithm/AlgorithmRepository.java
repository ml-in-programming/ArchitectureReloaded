package org.ml_methods_group.algorithm;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AlgorithmRepository {
    private static final List<Class<? extends Algorithm>> ALGORITHMS = Arrays.asList(ARI.class, AKMeans.class,
            CCDA.class, HAC.class, MRI.class);

    @NotNull
    @Contract(pure = true)
    public static List<Class<? extends Algorithm>> getAvailableAlgorithms() {
        return Collections.unmodifiableList(ALGORITHMS);
    }

    public static String[] getAvailableAlgorithmNames() {
        return ALGORITHMS.stream().map(Class::getSimpleName).toArray(String[]::new);
    }
}
