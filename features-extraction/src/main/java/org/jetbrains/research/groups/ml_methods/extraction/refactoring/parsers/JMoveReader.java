package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JMoveReader implements RefactoringsReader {
    @Override
    public List<RefactoringTextRepresentation> read(Path refactoringsPath) throws IOException {
        return parseLines(Files.lines(refactoringsPath));
    }

    @Override
    public List<RefactoringTextRepresentation> read(InputStream inputStream) {
        return parseLines(new BufferedReader(new InputStreamReader(inputStream)).lines());
    }

    private List<RefactoringTextRepresentation> parseLines(Stream<String> lines) {
        return lines.filter(line -> line.startsWith("method")).
                map(line -> new RefactoringTextRepresentation(getMethodPackage(line), getMethodName(line),
                        getMethodParams(line), getClassQualifiedName(line))).
                collect(Collectors.toList());
    }

    private List<String> getMethodParams(String line) {
        List<String> params = Arrays.asList(line.split("\\(")[1].split("\\)")[0].split(",(?=[a-zA-Z])"));
        return params.size() == 1 && params.get(0).equals("") ? Collections.emptyList() : params;
    }

    private String getClassQualifiedName(String line) {
        String[] words = line.split(" ");
        return words[words.length - 1];
    }

    private String getMethodName(String line) {
        return line.split(" ")[1].split("::")[1].split("\\(")[0];
    }

    private String getMethodPackage(String line) {
        return line.split(" ")[1].split("::")[0];
    }
}
