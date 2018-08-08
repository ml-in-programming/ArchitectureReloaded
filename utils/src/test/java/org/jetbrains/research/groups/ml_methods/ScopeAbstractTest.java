package org.jetbrains.research.groups.ml_methods;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ScopeAbstractTest extends LightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return getTestDataPathObject().toString();
    }

    protected AnalysisScope createScope() throws IOException {
        return createScope(
            Files.walk(getTestDataPathObject())
                 .filter(Files::isRegularFile)
                 .map(Path::toString)
                 .collect(Collectors.toList())
                 .toArray(new String[0])
        );
    }

    protected AnalysisScope createScope(String... files) {
        final List<VirtualFile> virtualFiles = Arrays.stream(files)
                .map(this::loadFile)
                .collect(Collectors.toList());
        return new AnalysisScope(myFixture.getProject(), virtualFiles);
    }

    private @NotNull VirtualFile loadFile(@NotNull String name) {
        final String fullName = Paths.get(getTestName(true), name).toString();
        return myFixture.copyFileToProject(name, fullName);
    }

    private @NotNull Path getTestDataPathObject() {
        return Paths.get(
            "src",
            "test",
            "resources",
            "testCases",
            getTestName(true)
        );
    }
}
