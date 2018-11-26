package org.jetbrains.research.groups.ml_methods.refactoring;

import org.jetbrains.research.groups.ml_methods.ScopeAbstractTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RefactoringsFinderTest extends ScopeAbstractTest {
    public void testFindRefactorings() {
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
        List<MoveMethodRefactoring> found = RefactoringsFinder.find(createScope("A.java", "B.java"), refactoringsToFind);
        List<RefactoringTextRepresentation> foundTextual = found.stream()
                .map(JBRefactoringTextRepresentation::new)
                .collect(Collectors.toList());
        assertEquals(new HashSet<>(refactoringsToFind), new HashSet<>(foundTextual));
    }
}