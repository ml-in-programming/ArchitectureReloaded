package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiType;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.TextFormRefactoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.System.out;

public class ParserForJMoveDataSet implements RefactoringsFileParser {
    @Override
    public Set<TextFormRefactoring> parse(Path refactoringsPath) throws IOException {
        return Files.lines(refactoringsPath).
                filter(line -> line.startsWith("method")).
                map(line -> new TextFormRefactoring(getMethodPackage(line), getMethodName(line),
                        getMethodParams(line), getClassQualifiedName(line))).
                collect(Collectors.toSet());
    }

    private List<String> getMethodParams(String line) {
        List<String> params = Arrays.asList(line.split("\\(")[1].split("\\)")[0].split(","));
        return params.size() == 1 && params.get(0).equals("") ? Collections.emptyList() : params;
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

    public static String getRefactoringInTextForm(Refactoring refactoring) {
        TextFormRefactoring textFormRefactoring = refactoring.toTextFormRefactoring();
        StringBuilder s = new StringBuilder();
        s.append("method ");
        s.append(textFormRefactoring.getMethodPackageWithClass());
        s.append("::");
        s.append(textFormRefactoring.getMethodName());
        s.append("(");
        final String[] parameters = textFormRefactoring.getParamsClasses().toArray(new String[0]);
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                s.append(',');
            }
            s.append(parameters[i]);
        }
        s.append(')');
        s.append(":RETURN_TYPE_NOT_NEEDED need move to ");
        s.append(textFormRefactoring.getTargetClassQualifiedName());
        return s.toString();
    }
}
