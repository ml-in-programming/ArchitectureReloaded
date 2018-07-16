package org.jetbrains.research.groups.ml_methods.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IdentifierTokenizer {
    @NotNull
    public static List<String> tokenize(@NotNull String identifier) {
        if (identifier.startsWith("_")) {
            identifier = identifier.substring(1);
        }
        List<String> tokenizedString = new LinkedList<>();
        String regexpSplit;
        if (identifier.toUpperCase().equals(identifier)) {
            regexpSplit = "_";
        }
        else {
            regexpSplit = "(?=\\p{Lu})";
        }
        tokenizedString.addAll(Arrays.asList(identifier.split(regexpSplit)));
        return tokenizedString.stream().map(String::toLowerCase).collect(Collectors.toList());
    }
}
