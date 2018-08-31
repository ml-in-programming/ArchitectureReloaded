package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.openapi.project.Project;
import org.jetbrains.research.groups.ml_methods.refactoring.MoveToClassRefactoring;

import java.util.List;

class ProjectToEvaluate {
    private final Project project;
    private final List<MoveToClassRefactoring> goodRefactorings;
    private final List<MoveToClassRefactoring> badRefactorings;

    Project getProject() {
        return project;
    }

    List<MoveToClassRefactoring> getGoodRefactorings() {
        return goodRefactorings;
    }

    List<MoveToClassRefactoring> getBadRefactorings() {
        return badRefactorings;
    }

    ProjectToEvaluate(Project project, List<MoveToClassRefactoring> goodRefactorings, List<MoveToClassRefactoring> badRefactorings) {
        this.project = project;
        this.goodRefactorings = goodRefactorings;
        this.badRefactorings = badRefactorings;
    }
}
