package org.jetbrains.research.groups.ml_methods.vectorization;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface Vectorization {
    void vectorizeAndSave(@NotNull Path datasetPath);

    void vectorizeSingleProject(@NotNull Path projectPath, @NotNull Path pathToSaveResults) throws IOException, CannotOpenProjectException;

    @NotNull
    List<? extends Vector> vectorize(@NotNull Path projectPath) throws CannotOpenProjectException;
}
