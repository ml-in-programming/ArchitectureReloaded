/*
 *  Copyright 2017 Machine Learning Methods in Software Engineering Research Group
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package vector.model;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.plugin.RefactoringExecutionContext;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;

import java.io.IOException;
import java.util.*;

public class RefactoringDetectionTest extends LightCodeInsightFixtureTestCase {
    private Project project;
    private AnalysisScope analysisScope;
    private MetricsProfile profile;

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected String getTestDataPath() {
        return "testdata/" + getTestName(true);
    }

    public void testMoveMethod() throws IOException {
        final VirtualFile file1 = myFixture.copyFileToProject("ClassA.java");
        final VirtualFile file2 = myFixture.copyFileToProject("ClassB.java");

        project = myFixture.getProject();
        analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , RefactoringDetectionTest::calculateMoveMethodRefactorings);
    }

    private static void calculateMoveMethodRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(6, context.getMethodsCount());
        assertEquals(4, context.getFieldsCount());

        final Map<String, String> refactoringsCCDA = context.calculateCCDA();
        assertEquals(1, refactoringsCCDA.size());
        assertEquals("ClassB.methodB1()", refactoringsCCDA.keySet().toArray()[0]);
        assertEquals("ClassA", refactoringsCCDA.get("ClassB.methodB1()"));

        final Map<String, String> refactoringsMRI = context.calculateMRI();
        assertEquals(1, refactoringsMRI.size());
        assertEquals("ClassB.methodB1()", refactoringsMRI.keySet().toArray()[0]);
        assertEquals("ClassA", refactoringsMRI.get("ClassB.methodB1()"));


//        final Set<String> common = new HashSet<>(refactoringsCCDA.keySet());
//        common.retainAll(refactoringsMRI.keySet());
//        System.out.println("Common for ARI and CCDA: ");
//        for (String move : common) {
//            System.out.print(move + " to ");
//            System.out.print(refactoringsCCDA.get(move));
//            if (!refactoringsMRI.get(move).equals(refactoringsCCDA.get(move))) {
//                System.out.print(" vs " + refactoringsMRI.get(move));
//            }
//            System.out.println();
//        }
//        System.out.println();
//
//        assertEquals(new HashSet(Collections.singletonList("ClassB.methodB1()")), common);

        final Map<String, String> refactoringsAKMeans = context.calculateAKMeans();

//        Set<String> refactoringsARIEC = new HashSet<>(refactoringsAKMeans.keySet());
//        refactoringsARIEC.retainAll(refactoringsMRI.keySet());
//        System.out.println("Common for ARI and EC: ");
//        for (String move : refactoringsARIEC) {
//            System.out.print(move + " to ");
//            System.out.print(refactoringsAKMeans.get(move));
//            if (!refactoringsMRI.get(move).equals(refactoringsAKMeans.get(move))) {
//                System.out.print(" vs " + refactoringsMRI.get(move));
//            }
//            System.out.println();
//        }
//        System.out.println();

        // TODO: make AKMeans more deterministic somehow and get some assertions here
//        assertEquals(new HashSet(Collections.singletonList("ClassB.methodB1()")), refactoringsARIEC);

        final Map<String, String> refactoringsHAC = context.calculateHAC();

        final Map<String, String> expectedHAC = new HashMap<>();
        expectedHAC.put("ClassB.methodB1()", "ClassA");
        assertEquals(expectedHAC, refactoringsHAC);

        final Map<String, String> refactoringsARI = context.calculateARI();
        assertEquals(expectedHAC, refactoringsARI);
    }
}