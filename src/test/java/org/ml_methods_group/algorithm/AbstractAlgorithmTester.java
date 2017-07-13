/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractAlgorithmTester extends LightCodeInsightFixtureTestCase {
    private volatile MetricsProfile profile;

    @Override
    protected String getTestDataPath() {
        return "testdata/src/" + getTestName(true);
    }
    protected VirtualFile loadFile(String name) {
        final String fullName = getTestName(true) + "/" + name;
        return myFixture.copyFileToProject(name, fullName);
    }

    protected MetricsProfile getProfile() {
        if (profile == null) {
            profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");
        }
        return profile;
    }

    protected AnalysisScope createScope(String... files) {
        final List<VirtualFile> virtualFiles = Arrays.stream(files)
                .map(this::loadFile)
                .collect(Collectors.toList());
        return new AnalysisScope(myFixture.getProject(), virtualFiles);
    }

    protected static void checkStructure(RefactoringExecutionContext context, int classes, int methods, int fields) {
        assertEquals(classes, context.getClassCount());
        assertEquals(methods, context.getMethodsCount());
        assertEquals(fields, context.getFieldsCount());
    }

    protected RefactoringExecutionContext createContext(AnalysisScope scope) {
        return new RefactoringExecutionContext(myFixture.getProject(), scope, getProfile());
    }
}
