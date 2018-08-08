package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import org.jetbrains.research.groups.ml_methods.ScopeAbstractTest;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.readers.RefactoringsReaders;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RefactoringsLoaderTest extends ScopeAbstractTest {
    public void testFindRefactorings() throws IOException {
        // TODO: JDK is not loaded, fix it. That is why for example Integer if not qualified (supposed to be java.lang.Integer).
        List<RefactoringTextRepresentation> refactoringsToFind =
                Arrays.asList(
                        new JBRefactoringTextRepresentation(
                                "findRefactorings.A", "a",
                                Collections.singletonList("int"), "findRefactorings.B"
                        ),
                        new JBRefactoringTextRepresentation(
                                "findRefactorings.B", "b",
                                Arrays.asList("Integer", "List<Integer>"), "findRefactorings.A"
                        )
                );
        List<MoveMethodRefactoring> found = RefactoringsLoader.load(Paths.get(getTestDataPath(), "good"), RefactoringsReaders.getJBReader(), createScope("A.java", "B.java"));
        List<RefactoringTextRepresentation> foundTextual = found.stream()
                .map(JBRefactoringTextRepresentation::new)
                .collect(Collectors.toList());
        assertEquals(new HashSet<>(refactoringsToFind), new HashSet<>(foundTextual));
    }
}