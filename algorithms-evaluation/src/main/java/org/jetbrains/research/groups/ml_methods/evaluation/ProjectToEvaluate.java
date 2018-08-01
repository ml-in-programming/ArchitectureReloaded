package org.jetbrains.research.groups.ml_methods.evaluation;

import com.intellij.openapi.project.Project;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.Refactoring;

import java.util.List;

public class ProjectToEvaluate {
    private final Project project;
    private final List<Refactoring> goodRefactorings;
    private final List<Refactoring> badRefactorings;

    public Project getProject() {
        return project;
    }

    public List<Refactoring> getGoodRefactorings() {
        return goodRefactorings;
    }

    public List<Refactoring> getBadRefactorings() {
        return badRefactorings;
    }

    public ProjectToEvaluate(Project project, List<Refactoring> goodRefactorings, List<Refactoring> badRefactorings) {
        this.project = project;
        this.goodRefactorings = goodRefactorings;
        this.badRefactorings = badRefactorings;
    }
}
