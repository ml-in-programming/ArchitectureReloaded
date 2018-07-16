package org.jetbrains.research.groups.ml_methods;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class ScopeAbstractTest extends LightCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return Paths.get(
            "src",
            "test",
            "resources",
            "testCases",
            getTestName(true)
        ).toString();
    }

    protected AnalysisScope createScope(String... files) {
        final List<VirtualFile> virtualFiles = Arrays.stream(files)
                .map(this::loadFile)
                .collect(Collectors.toList());
        return new AnalysisScope(myFixture.getProject(), virtualFiles);
    }

    @NotNull
    private VirtualFile loadFile(@NotNull String name) {
        final String fullName = Paths.get(getTestName(true), name).toString();
        return myFixture.copyFileToProject(name, fullName);
    }
}
