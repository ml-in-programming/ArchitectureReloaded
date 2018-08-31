package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ProjectEvaluationResultTest {
    private static final Algorithm TEST_ALGORITHM = mock(Algorithm.class);

    @Test
    public void onlyGoodRefactorings() {
        MoveToClassRefactoring foundGood = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring good = new CalculatedRefactoring(foundGood, 0.5);
        MoveToClassRefactoring notFoundGood = mock(MoveToClassRefactoring.class);
        ProjectEvaluationResult projectEvaluationResult = new ProjectEvaluationResult(
                Collections.singletonList(good), Arrays.asList(foundGood, notFoundGood),
                Collections.emptyList(), TEST_ALGORITHM
        );
        assertEquals(0, projectEvaluationResult.getNumberOfBad());
        assertEquals(2, projectEvaluationResult.getNumberOfGood());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundBad());
        assertEquals(1, projectEvaluationResult.getNumberOfFoundGood());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Arrays.asList(1 - 0.5, 1 - 0.0), projectEvaluationResult.getErrors());
    }

    @Test
    public void onlyBadRefactorings() {
        MoveToClassRefactoring notFoundBad1 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring notFoundBad2 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring foundBad = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring bad = new CalculatedRefactoring(foundBad, 0.5);
        ProjectEvaluationResult projectEvaluationResult = new ProjectEvaluationResult(
                Collections.singletonList(bad), Collections.emptyList(),
                Arrays.asList(foundBad, notFoundBad1, notFoundBad2), TEST_ALGORITHM
        );
        assertEquals(3, projectEvaluationResult.getNumberOfBad());
        assertEquals(0, projectEvaluationResult.getNumberOfGood());
        assertEquals(1, projectEvaluationResult.getNumberOfFoundBad());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundGood());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Arrays.asList(0.5 - 0.0, 0.0 - 0.0, 0.0 - 0.0), projectEvaluationResult.getErrors());
    }

    @Test
    public void onlyOtherRefactorings() {
        MoveToClassRefactoring notFoundOther = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring foundOther1 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring other1 = new CalculatedRefactoring(foundOther1, 0.5);
        MoveToClassRefactoring foundOther2 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring other2 = new CalculatedRefactoring(foundOther2, 0.1);
        ProjectEvaluationResult projectEvaluationResult = new ProjectEvaluationResult(
                Arrays.asList(other1, other2), Collections.emptyList(), Collections.emptyList(), TEST_ALGORITHM
        );
        assertEquals(0, projectEvaluationResult.getNumberOfBad());
        assertEquals(0, projectEvaluationResult.getNumberOfGood());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundBad());
        assertEquals(0, projectEvaluationResult.getNumberOfFoundGood());
        assertEquals(2, projectEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Collections.emptyList(), projectEvaluationResult.getErrors());
    }

    @Test
    public void allTypesRefactorings() {
        // bad
        MoveToClassRefactoring notFoundBad1 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring notFoundBad2 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring foundBad1 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring bad1 = new CalculatedRefactoring(foundBad1, 0.5);
        MoveToClassRefactoring foundBad2 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring bad2 = new CalculatedRefactoring(foundBad2, 0.1);
        MoveToClassRefactoring foundBad3 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring bad3 = new CalculatedRefactoring(foundBad3, 1);
        // good
        MoveToClassRefactoring notFoundGood1 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring notFoundGood2 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring notFoundGood3 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring foundGood1 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring good1 = new CalculatedRefactoring(foundGood1, 0);
        MoveToClassRefactoring foundGood2 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring good2 = new CalculatedRefactoring(foundGood2, 0.9);
        // other
        MoveToClassRefactoring notFoundOther1 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring notFoundOther2 = mock(MoveToClassRefactoring.class);
        MoveToClassRefactoring foundOther1 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring other1 = new CalculatedRefactoring(foundOther1, 0.8);
        MoveToClassRefactoring foundOther2 = mock(MoveToClassRefactoring.class);
        CalculatedRefactoring other2 = new CalculatedRefactoring(foundOther2, 0.4);

        ProjectEvaluationResult projectEvaluationResult = new ProjectEvaluationResult(
                Arrays.asList(bad1, bad2, bad3, good1, good2, other1, other2),
                Arrays.asList(foundGood1, foundGood2, notFoundGood1, notFoundGood2, notFoundGood3),
                Arrays.asList(foundBad1, foundBad2, foundBad3, notFoundBad1, notFoundBad2), TEST_ALGORITHM
        );
        assertEquals(5, projectEvaluationResult.getNumberOfBad());
        assertEquals(5, projectEvaluationResult.getNumberOfGood());
        assertEquals(3, projectEvaluationResult.getNumberOfFoundBad());
        assertEquals(2, projectEvaluationResult.getNumberOfFoundGood());
        assertEquals(2, projectEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Arrays.asList(0.5 - 0.0, 0.1 - 0.0, 1 - 0.0, 0.0 - 0.0,
                0.0 - 0.0, 1 - 0.0, 1 - 0.9,
                1 - 0.0, 1 - 0.0, 1 - 0.0), projectEvaluationResult.getErrors());
    }
}