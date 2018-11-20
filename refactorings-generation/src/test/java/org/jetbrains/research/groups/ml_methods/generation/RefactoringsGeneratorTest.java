package org.jetbrains.research.groups.ml_methods.generation;

import com.intellij.analysis.AnalysisScope;
import org.jetbrains.research.groups.ml_methods.ScopeAbstractTest;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory;
import org.jetbrains.research.groups.ml_methods.generation.constraints.GenerationConstraintsFactory.GenerationConstraintType;
import org.jetbrains.research.groups.ml_methods.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveMethodRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringTextRepresentation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class RefactoringsGeneratorTest extends ScopeAbstractTest {
    public void testGenerateRefactorings() {
        AnalysisScope scope = createScope("A.java", "B.java");
        List<RefactoringTextRepresentation> allowedRefactoringsToGenerate = Arrays.asList(
                        new JBRefactoringTextRepresentation("generateRefactorings.A", "move1",
                                Collections.emptyList(), "generateRefactorings.B"),
                        new JBRefactoringTextRepresentation("generateRefactorings.A", "move2",
                                Collections.singletonList("int"), "generateRefactorings.B")
        );
        MoveMethodRefactoring generatedRefactoring =
                RefactoringsGenerator.generate(
                        GenerationConstraintsFactory.get(GenerationConstraintType.ACCEPT_RELEVANT_PROPERTIES),
                        scope
                );
        assertTrue(allowedRefactoringsToGenerate.contains(new JBRefactoringTextRepresentation(generatedRefactoring)));
        List<MoveMethodRefactoring> generatedRefactorings =
                RefactoringsGenerator.generate(
                        GenerationConstraintsFactory.get(GenerationConstraintType.ACCEPT_RELEVANT_PROPERTIES),
                        2,
                        scope
                );
        assertEquals(new HashSet<>(allowedRefactoringsToGenerate),
                generatedRefactorings.stream().map(JBRefactoringTextRepresentation::new).collect(Collectors.toSet()));
    }
}