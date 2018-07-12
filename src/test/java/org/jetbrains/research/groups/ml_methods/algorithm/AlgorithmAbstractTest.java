/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.research.groups.ml_methods.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.Entity;
import org.jetbrains.research.groups.ml_methods.refactoring.RefactoringExecutionContext;
import org.jetbrains.research.groups.ml_methods.utils.MetricsProfilesUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public abstract class AlgorithmAbstractTest extends LightCodeInsightFixtureTestCase {
    protected final TestCasesCheckers testCasesChecker = new TestCasesCheckers(getAlgorithmName());

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases/" + getTestName(true);
    }

    @NotNull
    private VirtualFile loadFile(@NotNull String name) {
        final String fullName = getTestName(true) + "/" + name;
        return myFixture.copyFileToProject(name, fullName);
    }

    protected AnalysisScope createScope(String... files) {
        final List<VirtualFile> virtualFiles = Arrays.stream(files)
                .map(this::loadFile)
                .collect(Collectors.toList());
        return new AnalysisScope(myFixture.getProject(), virtualFiles);
    }

    protected RefactoringExecutionContext createContext(AnalysisScope scope, String algorithmName, Consumer<RefactoringExecutionContext> checker) {
        MetricsProfile profile = MetricsProfilesUtil.createProfile("test_profile", Entity.getRequestedMetrics());
        return new RefactoringExecutionContext(myFixture.getProject(), scope, profile,
                Collections.singletonList(algorithmName), true,
                checker);
    }

    protected void executeTest(Consumer<RefactoringExecutionContext> checker, String... files) {
        AnalysisScope scope = createScope(files);
        createContext(scope, getAlgorithmName(), checker).executeSynchronously();
    }

    protected abstract String getAlgorithmName();
}
