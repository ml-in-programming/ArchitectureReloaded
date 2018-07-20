package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.TextFormRefactoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ParserForJMoveDataSet implements RefactoringsFileParser {
    @Override
    public Set<TextFormRefactoring> parse(Path refactoringsPath) throws IOException {
        return Files.lines(refactoringsPath).
                filter(line -> line.startsWith("method")).
                map(line -> new TextFormRefactoring(getMethodPackage(line), getMethodName(line),
                        Collections.emptyList(), getClassQualifiedName(line))).
                collect(Collectors.toSet());
    }

    private String getClassQualifiedName(String line) {
        return line.split(" ")[5];
    }

    private String getMethodName(String line) {
        return line.split(" ")[1].split("::")[1].split("\\(")[0];
    }

    private String getMethodPackage(String line) {
        return line.split(" ")[1].split("::")[0];
    }
}
