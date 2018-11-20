package org.jetbrains.research.groups.ml_methods.vectorization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.sixrr.metrics.utils.ProjectUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract public class AbstractVectorization implements Vectorization {
    private static final @NotNull
    Gson JSON_CONVERTER = new GsonBuilder().setPrettyPrinting().create();
    private static final @NotNull
    Logger LOGGER = Logger.getLogger(VectorizationApplicationStarter.class);

    @Override
    public void vectorizeAndSave(@NotNull Path datasetPath) {
        for (File projectPath : Objects.requireNonNull(datasetPath.resolve("projects").toFile().listFiles())) {
            try {
                if (!projectPath.isDirectory()) {
                    throw new IOException(projectPath + "is not a directory!");
                }
                vectorizeSingleProject(projectPath.toPath(), projectPath.toPath());
            } catch (IOException e) {
                String errorMessage = "Error during saving vectorization result to folder " + projectPath;
                VectorizationApplicationStarter.logError(e, LOGGER, errorMessage);
                VectorizationApplicationStarter.showError(e, errorMessage);
            } catch (CannotOpenProjectException e) {
                String errorMessage = "Error during opening project: " + projectPath;
                VectorizationApplicationStarter.logError(e, LOGGER, errorMessage);
                VectorizationApplicationStarter.showError(e, errorMessage);
            }
        }
    }

    @Override
    public void vectorizeSingleProject(@NotNull Path projectPath, @NotNull Path pathToSaveResults)
            throws IOException, CannotOpenProjectException {
        List<? extends Vector> vectors = vectorize(projectPath);
        Files.write(pathToSaveResults.resolve("vector"), Collections.singleton(JSON_CONVERTER.toJson(vectors)));
    }

    @NotNull
    @Override
    public List<? extends Vector> vectorize(@NotNull Path projectPath) throws CannotOpenProjectException {
        projectPath = projectPath.resolve("project");
        final Project project = ProjectUtils.loadProjectWithAllDependencies(projectPath);
        if (project == null) {
            final String errorMessage = "Cannot open project. Check that path is correct. Project: " + projectPath;
            throw new CannotOpenProjectException(errorMessage);
        }
        return vectorize(new AnalysisScope(project));
    }

    @NotNull
    abstract protected List<? extends Vector> vectorize(@NotNull AnalysisScope scope);
}
