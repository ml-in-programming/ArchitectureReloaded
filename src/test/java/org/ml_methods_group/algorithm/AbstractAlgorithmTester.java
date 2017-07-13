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

    private MetricsProfile profile;

    public abstract Map<String, String> applyAlgorithm(RefactoringExecutionContext context);

    @Override
    protected String getTestDataPath() {
        return "testdata/src/" + getTestName(true);
    }

    private VirtualFile loadFile(String name) {
        final String fullName = getTestName(true) + "/" + name;
        return myFixture.copyFileToProject(name, fullName);
    }

    private MetricsProfile getProfile() {
        if (profile == null) {
            profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");
        }
        return profile;
    }

    private AnalysisScope createScope(String... files) {
        final List<VirtualFile> virtualFiles = Arrays.stream(files)
                .map(this::loadFile)
                .collect(Collectors.toList());
        return new AnalysisScope(myFixture.getProject(), virtualFiles);
    }

    private static void checkStructure(RefactoringExecutionContext context, int classes, int methods, int fields) {
        assertEquals(classes, context.getClassCount());
        assertEquals(methods, context.getMethodsCount());
        assertEquals(fields, context.getFieldsCount());
    }

    private RefactoringExecutionContext createContext(AnalysisScope scope) {
        return new RefactoringExecutionContext(myFixture.getProject(), scope, getProfile());
    }

    public void testMoveMethod() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 6, 4);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertEquals("moveMethod.ClassB.methodB1()", refactorings.keySet().toArray()[0]);
        assertEquals("moveMethod.ClassA", refactorings.get("moveMethod.ClassB.methodB1()"));
    }

    // failed for ARI, MRI
    public void ignored_testMoveField() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 11, 2);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA1");
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA2");
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA1"));
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA2"));
    }

    // Failed for ARI, HACT, MRI
    public void ignored_testMoveTogether() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 8, 4);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB1()");
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB2()");
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB1()"));
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB2()"));
    }

    // Failed for ARI, MRI
    public void ignored_testRecursiveMethod() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 5, 2);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "recursiveMethod.ClassA.methodA1()");
        assertEquals("recursiveMethod.ClassB", refactorings.get("recursiveMethod.ClassA.methodA1()"));
    }

    // Failed for ARI, MRI
    public void ignored_testCrossReferencesMethods() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 2, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        if (refactorings.containsKey("crossReferencesMethods.ClassA.methodA1()")) {
            assertEquals("crossReferencesMethods.ClassB",
                    refactorings.get("crossReferencesMethods.ClassA.methodA1()"));
        } else {
            assertContainsElements(refactorings.keySet(), "crossReferencesMethods.ClassB.methodB1()");
            assertEquals("crossReferencesMethods.ClassA",
                    refactorings.get("crossReferencesMethods.ClassB.methodB1()"));
        }
    }

    // Failed for all algorithms
    public void ignored_testReferencesOnly() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 4, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        if (refactorings.containsKey("referencesOnly.ClassB.mainF()")) {
            assertEquals(1, refactorings.size());
            assertContainsElements(refactorings.keySet(), "referencesOnly.ClassB.mainF()");
            assertEquals("referencesOnly.ClassA", refactorings.get("referencesOnly.ClassB.mainF()"));
        } else {
            assertEquals(2, refactorings.size());
            assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething1()");
            assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething2()");
            assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething1()"));
            assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething2()"));
        }
    }

    // Failed for ARI, HACT, MRI
    public void ignored_testCallFromNested() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 3, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "callFromNested.ClassB.methodB1()");
        assertOneOf(refactorings.get("callFromNested.ClassB.methodB1()"),
                "callFromNested.ClassA", "callFromNested.ClassA.Nested");
    }

    public void testDontMoveConstructor() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    // Failed for CCDA
    public void ignored_testDontMoveOverridden() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    // Failed for ARI, MRI
    public void ignored_testCircularDependency() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 3, 3, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        final String moveToClass;
        if (!refactorings.containsKey("circularDependency.ClassA.fooA()")) {
            moveToClass = "circularDependency.ClassA";
        } else if (!refactorings.containsKey("circularDependency.ClassB.fooB()")) {
            moveToClass = "circularDependency.ClassB";
        } else {
            moveToClass = "circularDependency.ClassC";
        }
        assertContainsElements(refactorings.values(), moveToClass, moveToClass);
    }

    // Failed for CCDA
    public void ignored_testDontMoveAbstract() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 3, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    // Failed for ARI, CCDA, MRI
    public void ignored_testTriangularDependence() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 3, 8, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "triangularDependence.ClassB.methodToMove()",
                "triangularDependence.ClassC.methodToMove()");
        assertEquals("triangularDependence.ClassA",
                refactorings.get("triangularDependence.ClassB.methodToMove()"));
        assertEquals("triangularDependence.ClassA",
                refactorings.get("triangularDependence.ClassC.methodToMove()"));
    }

    // Failed for ARI, HACT, MRI
    public void ignored_testPriority() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        RefactoringExecutionContext context = createContext(analysisScope);
        checkStructure(context, 2, 9, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "priority.ClassA.methodA1()");
        assertEquals("priority.ClassB", refactorings.get("priority.ClassA.methodA1()"));
    }
}
