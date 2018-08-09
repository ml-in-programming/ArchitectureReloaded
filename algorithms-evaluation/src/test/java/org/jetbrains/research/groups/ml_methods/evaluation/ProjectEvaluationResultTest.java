package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class ProjectEvaluationResultTest {
    private static final Algorithm TEST_ALGORITHM = mock(Algorithm.class);

    @Test
    public void onlyGoodRefactorings() {
        Refactoring foundGood = mock(Refactoring.class);
        CalculatedRefactoring good = new CalculatedRefactoring(foundGood, 0.5);
        Refactoring notFoundGood = mock(Refactoring.class);
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
        Refactoring notFoundBad1 = mock(Refactoring.class);
        Refactoring notFoundBad2 = mock(Refactoring.class);
        Refactoring foundBad = mock(Refactoring.class);
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
        Refactoring notFoundOther = mock(Refactoring.class);
        Refactoring foundOther1 = mock(Refactoring.class);
        CalculatedRefactoring other1 = new CalculatedRefactoring(foundOther1, 0.5);
        Refactoring foundOther2 = mock(Refactoring.class);
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
        Refactoring notFoundBad1 = mock(Refactoring.class);
        Refactoring notFoundBad2 = mock(Refactoring.class);
        Refactoring foundBad1 = mock(Refactoring.class);
        CalculatedRefactoring bad1 = new CalculatedRefactoring(foundBad1, 0.5);
        Refactoring foundBad2 = mock(Refactoring.class);
        CalculatedRefactoring bad2 = new CalculatedRefactoring(foundBad2, 0.1);
        Refactoring foundBad3 = mock(Refactoring.class);
        CalculatedRefactoring bad3 = new CalculatedRefactoring(foundBad3, 1);
        // good
        Refactoring notFoundGood1 = mock(Refactoring.class);
        Refactoring notFoundGood2 = mock(Refactoring.class);
        Refactoring notFoundGood3 = mock(Refactoring.class);
        Refactoring foundGood1 = mock(Refactoring.class);
        CalculatedRefactoring good1 = new CalculatedRefactoring(foundGood1, 0);
        Refactoring foundGood2 = mock(Refactoring.class);
        CalculatedRefactoring good2 = new CalculatedRefactoring(foundGood2, 0.9);
        // other
        Refactoring notFoundOther1 = mock(Refactoring.class);
        Refactoring notFoundOther2 = mock(Refactoring.class);
        Refactoring foundOther1 = mock(Refactoring.class);
        CalculatedRefactoring other1 = new CalculatedRefactoring(foundOther1, 0.8);
        Refactoring foundOther2 = mock(Refactoring.class);
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