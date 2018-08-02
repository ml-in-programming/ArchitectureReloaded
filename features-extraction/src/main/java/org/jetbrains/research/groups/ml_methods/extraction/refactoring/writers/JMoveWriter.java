package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JMoveRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class JMoveWriter implements RefactoringsWriter {
    private static final String NAME = "JMove";

    @Override
    public String getName() {
        return NAME;
    }

    private String getRefactoringInJMoveFormat(JMoveRefactoringTextRepresentation refactoring) {
        StringBuilder s = new StringBuilder();
        s.append("method ");
        s.append(refactoring.getSourceClassQualifiedName());
        s.append("::");
        s.append(refactoring.getMethodName());
        s.append("(");
        final String[] parameters = refactoring.getParamsClasses().toArray(new String[0]);
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                s.append(',');
            }
            s.append(parameters[i]);
        }
        if (parameters.length == 0) {
            s.append("void");
        }
        s.append(')');
        s.append(":RETURN_TYPE_NOT_NEEDED need move to ");
        s.append(refactoring.getTargetClassQualifiedName());
        return s.toString();
    }

    private List<String> getRefactoringsInJMoveFormat(List<Refactoring> refactorings) {
        return refactorings.stream()
                .map(JMoveRefactoringTextRepresentation::new)
                .map(this::getRefactoringInJMoveFormat)
                .collect(Collectors.toList());
    }

    @Override
    public void write(List<Refactoring> refactorings, Path refactoringsPath) throws IOException {
        Files.write(refactoringsPath, getRefactoringsInJMoveFormat(refactorings));
    }

    @Override
    public void write(List<Refactoring> refactorings, OutputStream outputStream) throws IOException {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            for (String refactoring : getRefactoringsInJMoveFormat(refactorings)) {
                bufferedWriter.write(refactoring);
                bufferedWriter.newLine();
            }
        }
    }
}
