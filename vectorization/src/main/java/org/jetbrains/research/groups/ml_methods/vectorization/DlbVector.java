package org.jetbrains.research.groups.ml_methods.vectorization;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Math.min;

public class DlbVector implements Vector {
    private static final int NAME_LENGTH = 5;
    private static final @NotNull String DEFAULT_NAME = "*";
    private final @NotNull String[] methodName = new String[NAME_LENGTH];
    private final @NotNull String[] sourceClassName = new String[NAME_LENGTH];
    private final @NotNull String[] targetClassName = new String[NAME_LENGTH];
    private final double distanceWithSourceClass;
    private final double distanceWithTargetClass;

    public DlbVector(@NotNull String methodName, @NotNull String sourceClassName, @NotNull String targetClassName,
                     double distanceWithSourceClass, double distanceWithTargetClass) {
        List<String> method = tokenizeName(methodName);
        List<String> sourceClass = tokenizeName(sourceClassName);
        List<String> targetClass = tokenizeName(targetClassName);
        int skipMethod = NAME_LENGTH - min(method.size(), 5);
        int skipSourceClass = NAME_LENGTH - min(sourceClass.size(), 5);
        int skipTargetClass = NAME_LENGTH - min(targetClass.size(), 5);
        for (int i = 0; i < NAME_LENGTH; i++) {
            this.methodName[i] = getOrDefault(method, i - skipMethod, DEFAULT_NAME);
            this.sourceClassName[i] = getOrDefault(sourceClass, i - skipSourceClass, DEFAULT_NAME);
            this.targetClassName[i] = getOrDefault(targetClass, i - skipTargetClass, DEFAULT_NAME);
        }
        this.distanceWithSourceClass = distanceWithSourceClass;
        this.distanceWithTargetClass = distanceWithTargetClass;
    }

    @NotNull
    private static <T> T getOrDefault(List<T> list, int index, T defaultValue) {
        return 0 <= index && index < list.size() ? list.get(index) : defaultValue;
    }

    @NotNull
    private static List<String> tokenizeName(@NotNull String name) {
        if (name.startsWith("_")) {
            name = name.substring(1);
        }
        String regexpSplit;
        if (name.toUpperCase().equals(name)) {
            regexpSplit = "_";
        } else {
            regexpSplit = "(?=\\p{Lu})";
        }
        List<String> tokenizedString = new LinkedList<>(Arrays.asList(name.split(regexpSplit)));
        return tokenizedString.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
