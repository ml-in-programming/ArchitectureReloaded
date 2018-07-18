package org.jetbrains.research.groups.ml_methods.extraction.refactoring.parsers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.TextFormRefactoring;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RefactoringsFileParser {
    List<TextFormRefactoring> parse(Path refactoringsPath) throws IOException;
}
