package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.Algorithm;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;

import java.util.Collections;
import java.util.List;

public class AlgorithmEvaluator {
    @NotNull
    public static EvaluationResult evaluate(@NotNull AnalysisScope scope, @NotNull Algorithm algorithm,
                                            @NotNull List<Refactoring> good, @NotNull List<Refactoring> bad) {
        MetricsProfile profile =
                MetricsProfilesUtil.createProfile("evaluation_profile", algorithm.requiredMetrics());
        RefactoringExecutionContext context = new RefactoringExecutionContext(scope.getProject(), scope, profile,
                Collections.singletonList(algorithm), false, null);

        context.executeSynchronously();

        List<Refactoring> foundRefactorings = context.getAlgorithmResults().get(0).getRefactorings();
        return new ProjectEvaluationResult(foundRefactorings, good, bad);
    }

    @NotNull
    public static EvaluationResult evaluate(@NotNull Project project, @NotNull Algorithm algorithm,
                                            @NotNull List<Refactoring> good, @NotNull List<Refactoring> bad) {
        return evaluate(new AnalysisScope(project), algorithm, good, bad);
    }

    @NotNull
    public static EvaluationResult evaluate(@NotNull ProjectToEvaluate projectToEvaluate,
                                            @NotNull Algorithm algorithm) {
        return evaluate(projectToEvaluate.getProject(),
                algorithm,
                projectToEvaluate.getGoodRefactorings(),
                projectToEvaluate.getBadRefactorings());
    }
}
