package com.sixrr.metrics.utils;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ProjectUtils {
    private ProjectUtils() {
    }

    @Nullable
    public static Project loadProjectWithAllDependencies(Path projectPath) {
        for (Project openedProject : ProjectManager.getInstance().getOpenProjects()) {
            if (!ProjectManager.getInstance().closeProject(openedProject)) {
                throw new IllegalStateException("Cannot close previous project");
            }
            Disposer.dispose(openedProject);
            if (!openedProject.isDisposed()) {
                throw new IllegalStateException("Cannot dispose project");
            }
        }
        final Project project = ProjectUtil.openOrImport(projectPath.toAbsolutePath().toString(),
                null, false);
        System.out.println("Project: " + project);
        if (project != null) {
            System.out.println("SDK : " +  ProjectRootManager.getInstance(project).getProjectSdk());
            System.out.println("is project initialized : " +  project.isInitialized());
        }
        return project == null ||
                ProjectRootManager.getInstance(project).getProjectSdk() == null ||
                !project.isInitialized() ?
                null : project;
    }
}
