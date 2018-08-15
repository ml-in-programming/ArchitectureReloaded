package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.refactoring.CalculatedRefactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;

import java.util.Collections;
import java.util.List;

public class AlgorithmEvaluator {
    @NotNull
    static EvaluationResult evaluate(@NotNull AnalysisScope scope, @NotNull Algorithm algorithm,
                                            @NotNull List<MoveToClassRefactoring> good, @NotNull List<MoveToClassRefactoring> bad) {
        MetricsProfile profile =
                MetricsProfilesUtil.createProfile("evaluation_profile", algorithm.requiredMetrics());
        RefactoringExecutionContext context = new RefactoringExecutionContext(scope.getProject(), scope, profile,
                Collections.singletonList(algorithm), false, null);

        context.executeSynchronously();

        List<CalculatedRefactoring> foundRefactorings = context.getAlgorithmResults().get(0).getRefactorings();
        return new ProjectEvaluationResult(foundRefactorings, good, bad, algorithm);
    }

    @NotNull
    static EvaluationResult evaluate(@NotNull Project project, @NotNull Algorithm algorithm,
                                            @NotNull List<MoveToClassRefactoring> good, @NotNull List<MoveToClassRefactoring> bad) {
        return evaluate(new AnalysisScope(project), algorithm, good, bad);
    }

    @NotNull
    static EvaluationResult evaluate(@NotNull ProjectToEvaluate projectToEvaluate,
                                     @NotNull Algorithm algorithm) {
        return evaluate(projectToEvaluate.getProject(),
                algorithm,
                projectToEvaluate.getGoodRefactorings(),
                projectToEvaluate.getBadRefactorings());
    }
}
