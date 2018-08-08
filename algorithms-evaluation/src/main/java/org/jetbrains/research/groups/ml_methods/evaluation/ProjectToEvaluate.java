package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.openapi.project.Project;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;

import java.util.List;

class ProjectToEvaluate {
    private final Project project;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> badRefactorings;

    Project getProject() {
        return project;
    }

    List<Refactoring> getGoodRefactorings() {
        return goodRefactorings;
    }

    List<Refactoring> getBadRefactorings() {
        return badRefactorings;
    }

    ProjectToEvaluate(Project project, List<Refactoring> goodRefactorings, List<Refactoring> badRefactorings) {
        this.project = project;
        this.goodRefactorings = goodRefactorings;
        this.badRefactorings = badRefactorings;
    }
}
