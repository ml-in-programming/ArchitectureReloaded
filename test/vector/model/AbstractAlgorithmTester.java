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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.plugin.RefactoringExecutionContext;
import com.sixrr.metrics.profile.MetricsProfile;
import com.sixrr.metrics.profile.MetricsProfileRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
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

    private void runTest(Consumer<RefactoringExecutionContext> checker, AnalysisScope scope) {
        new RefactoringExecutionContext(myFixture.getProject(), scope, getProfile(), false, checker);
    }

    public void testMoveMethod() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkMoveMethodRefactorings, analysisScope);
    }

    public void testMoveField() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkMoveFieldRefactorings, analysisScope);
    }

    public void testMoveTogether() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkMoveTogetherRefactorings, analysisScope);
    }

    public void testRecursiveMethod() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkRecursiveMethodRefactorings, analysisScope);
    }

    public void testCrossReferencesMethods() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkCrossReferencesMethodsRefactorings, analysisScope);
    }

    public void testReferencesOnly() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkReferencesOnlyRefactorings, analysisScope);
    }

    public void testCallFromNested() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkCallFromNestedRefactorings, analysisScope);
    }

    public void testDontMoveConstructor() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkDontMoveConstructorRefactorings, analysisScope);
    }

    public void testDontMoveOverridden() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkDontMoveOverriddenRefactorings, analysisScope);
    }

    public void testCircularDependency() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        runTest(this::checkCircularDependencyRefactorings, analysisScope);
    }

    public void testDontMoveAbstract() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java");
        runTest(this::checkDontMoveAbstractRefactorings, analysisScope);
    }

    public void testTriangularDependence() throws IOException {
        final AnalysisScope analysisScope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        runTest(this::checkTriangularDependenceRefactorings, analysisScope);
    }

    private void checkMoveMethodRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 6, 4);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertEquals("moveMethod.ClassB.methodB1()", refactorings.keySet().toArray()[0]);
        assertEquals("moveMethod.ClassA", refactorings.get("moveMethod.ClassB.methodB1()"));
    }

    private void checkMoveFieldRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 11, 2);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA1");
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA2");
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA1"));
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA2"));
    }

    private void checkMoveTogetherRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 8, 4);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB1()");
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB2()");
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB1()"));
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB2()"));
    }

    private void checkRecursiveMethodRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 6, 4);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "recursiveMethod.ClassA.methodA1()");
        assertEquals("recursiveMethod.ClassB", refactorings.get("recursiveMethod.ClassA.methodA1()"));
    }

    private void checkCrossReferencesMethodsRefactorings(RefactoringExecutionContext context) {
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

    private void checkReferencesOnlyRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething1()");
        assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething2()");
        assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething1()"));
        assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething2()"));
    }

    private void checkCallFromNestedRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 3, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "callFromNested.ClassB.methodB1()");
        assertOneOf(refactorings.get("callFromNested.ClassB.methodB1()"),
                "callFromNested.ClassA", "callFromNested.ClassA.Nested");
    }

    private void checkDontMoveConstructorRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void checkDontMoveOverriddenRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void checkCircularDependencyRefactorings(RefactoringExecutionContext context) {
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

    private void checkDontMoveAbstractRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void checkTriangularDependenceRefactorings(RefactoringExecutionContext context) {
        checkStructure(context, 3, 6, 0);

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "triangularDependence.ClassB.methodToMove()",
                "triangularDependence.ClassC.methodToMove()");
        assertEquals("triangularDependence.ClassA",
                refactorings.get("triangularDependence.ClassB.methodToMove()"));
        assertEquals("triangularDependence.ClassA",
                refactorings.get("triangularDependence.ClassC.methodToMove()"));
    }
}
