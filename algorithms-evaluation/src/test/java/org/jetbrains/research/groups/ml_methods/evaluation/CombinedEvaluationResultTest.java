package org.jetbrains.research.groups.ml_methods.evaluation;

import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.lang.Math.pow;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CombinedEvaluationResultTest {
    private static final Algorithm TEST_ALGORITHM = mock(Algorithm.class);

    @Test
    public void addResult() {
        CombinedEvaluationResult combinedEvaluationResult = new CombinedEvaluationResult(TEST_ALGORITHM);
        assertEquals(0, combinedEvaluationResult.getNumberOfGood());
        assertEquals(0, combinedEvaluationResult.getNumberOfFoundGood());
        assertEquals(0, combinedEvaluationResult.getNumberOfBad());
        assertEquals(0, combinedEvaluationResult.getNumberOfFoundBad());
        assertEquals(0, combinedEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Collections.emptyList(), combinedEvaluationResult.getErrorSquares());

        ProjectEvaluationResult projectEvaluationResult1 = mock(ProjectEvaluationResult.class);
        when(projectEvaluationResult1.getAlgorithm()).thenReturn(TEST_ALGORITHM);
        when(projectEvaluationResult1.getNumberOfGood()).thenReturn(10);
        when(projectEvaluationResult1.getNumberOfFoundGood()).thenReturn(4);
        when(projectEvaluationResult1.getNumberOfBad()).thenReturn(6);
        when(projectEvaluationResult1.getNumberOfFoundBad()).thenReturn(2);
        when(projectEvaluationResult1.getNumberOfFoundOthers()).thenReturn(12);
        when(projectEvaluationResult1.getErrorSquares()).thenReturn(Arrays.asList(
                pow(1 - 0.5, 2), pow(1 - 0.9, 2), pow(1 - 0.7, 2), pow(1 - 0.85, 2),
                pow(0.8 - 0.0, 2), pow(0.2 - 0, 2)
        ));
        combinedEvaluationResult.addResult(projectEvaluationResult1);
        assertEquals(10, combinedEvaluationResult.getNumberOfGood());
        assertEquals(4, combinedEvaluationResult.getNumberOfFoundGood());
        assertEquals(6, combinedEvaluationResult.getNumberOfBad());
        assertEquals(2, combinedEvaluationResult.getNumberOfFoundBad());
        assertEquals(12, combinedEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Arrays.asList(
                pow(1 - 0.5, 2), pow(1 - 0.9, 2), pow(1 - 0.7, 2), pow(1 - 0.85, 2),
                pow(0.8 - 0.0, 2), pow(0.2 - 0, 2)), combinedEvaluationResult.getErrorSquares());
        assertEquals((double) 4 / 6, combinedEvaluationResult.getGoodPrecision(), 0);
        assertEquals((double) 4 / 10, combinedEvaluationResult.getGoodRecall(), 0);
        assertEquals((double) (6 - 2) / (10 + 6 - 4 - 2), combinedEvaluationResult.getBadPrecision(), 0);
        assertEquals((double) (6 - 2) / 6, combinedEvaluationResult.getBadRecall(), 0);
        assertEquals((pow(1 - 0.5, 2) + pow(1 - 0.9, 2) + pow(1 - 0.7, 2) + pow(1 - 0.85, 2) +
                pow(0.8 - 0.0, 2) + pow(0.2 - 0, 2)) / 6, combinedEvaluationResult.getMSE(), 0);

        ProjectEvaluationResult projectEvaluationResult2 = mock(ProjectEvaluationResult.class);
        when(projectEvaluationResult2.getAlgorithm()).thenReturn(TEST_ALGORITHM);
        when(projectEvaluationResult2.getNumberOfGood()).thenReturn(14);
        when(projectEvaluationResult2.getNumberOfFoundGood()).thenReturn(2);
        when(projectEvaluationResult2.getNumberOfBad()).thenReturn(10);
        when(projectEvaluationResult2.getNumberOfFoundBad()).thenReturn(1);
        when(projectEvaluationResult2.getNumberOfFoundOthers()).thenReturn(24);
        when(projectEvaluationResult2.getErrorSquares()).thenReturn(Arrays.asList(
                pow(1 - 0.2, 2), pow(1 - 0.1, 2),
                pow(0.9 - 0.0, 2)
        ));
        combinedEvaluationResult.addResult(projectEvaluationResult2);
        assertEquals(24, combinedEvaluationResult.getNumberOfGood());
        assertEquals(6, combinedEvaluationResult.getNumberOfFoundGood());
        assertEquals(16, combinedEvaluationResult.getNumberOfBad());
        assertEquals(3, combinedEvaluationResult.getNumberOfFoundBad());
        assertEquals(36, combinedEvaluationResult.getNumberOfFoundOthers());
        assertEquals(Arrays.asList(
                pow(1 - 0.5, 2), pow(1 - 0.9, 2), pow(1 - 0.7, 2), pow(1 - 0.85, 2),
                pow(0.8 - 0.0, 2), pow(0.2 - 0, 2), pow(1 - 0.2, 2), pow(1 - 0.1, 2),
                pow(0.9 - 0.0, 2)), combinedEvaluationResult.getErrorSquares());
        assertEquals((double) 6 / 9, combinedEvaluationResult.getGoodPrecision(), 0);
        assertEquals((double) 6 / 24, combinedEvaluationResult.getGoodRecall(), 0);
        assertEquals((double) (16 - 3) / (24 + 16 - 6 - 3), combinedEvaluationResult.getBadPrecision(), 0);
        assertEquals((double) (16 - 3) / 16, combinedEvaluationResult.getBadRecall(), 0);
        assertEquals((pow(1 - 0.5, 2) + pow(1 - 0.9, 2) + pow(1 - 0.7, 2) + pow(1 - 0.85, 2) +
                pow(0.8 - 0.0, 2) + pow(0.2 - 0, 2) + pow(1 - 0.2, 2) + pow(1 - 0.1, 2) + pow(0.9 - 0.0, 2)) / 9,
                combinedEvaluationResult.getMSE(),
                0);
    }
}