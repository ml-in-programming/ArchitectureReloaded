package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AlgorithmEvaluator {
    @NotNull
    private static List<CalculatedRefactoring> getTopRefactorings(@NotNull List<CalculatedRefactoring> calculatedRefactorings,
                                                           @NotNull Integer topRefactoringsBound) {
        List<CalculatedRefactoring> result = calculatedRefactorings.stream()
                .sorted(Comparator.comparingDouble(CalculatedRefactoring::getAccuracy).reversed())
                .limit(topRefactoringsBound)
                .collect(Collectors.toList());
        return result;
    }

    @NotNull
    static EvaluationResult evaluate(@NotNull AnalysisScope scope, @NotNull Algorithm algorithm,
                                     @NotNull List<Refactoring> good, @NotNull List<Refactoring> bad,
                                     @Nullable Integer topRefactoringsBound) {
        MetricsProfile profile =
                MetricsProfilesUtil.createProfile("evaluation_profile", algorithm.requiredMetrics());
        RefactoringExecutionContext context = new RefactoringExecutionContext(scope.getProject(), scope, profile,
                Collections.singletonList(algorithm), false, null);

        context.executeSynchronously();

        List<CalculatedRefactoring> foundRefactorings = context.getAlgorithmResults().get(0).getRefactorings();
        return new ProjectEvaluationResult(topRefactoringsBound == null ?
                foundRefactorings : getTopRefactorings(foundRefactorings, topRefactoringsBound), good, bad, algorithm);
    }

    @NotNull
    static EvaluationResult evaluate(@NotNull Project project, @NotNull Algorithm algorithm,
                                     @NotNull List<Refactoring> good, @NotNull List<Refactoring> bad,
                                     @Nullable Integer topRefactoringsBound) {
        return evaluate(new AnalysisScope(project), algorithm, good, bad, topRefactoringsBound);
    }

    @NotNull
    static EvaluationResult evaluate(@NotNull ProjectToEvaluate projectToEvaluate,
                                     @NotNull Algorithm algorithm,
                                     @Nullable Integer topRefactoringsBound) {
        return evaluate(projectToEvaluate.getProject(),
                algorithm,
                projectToEvaluate.getGoodRefactorings(),
                projectToEvaluate.getBadRefactorings(),
                topRefactoringsBound);
    }
}
