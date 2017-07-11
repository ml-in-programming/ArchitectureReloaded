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
import java.util.Arrays;
import java.util.Map;

/**
 * Created by Артём on 10.07.2017.
 */
public abstract class AbstractAlgorithmTester extends LightCodeInsightFixtureTestCase {

    private MetricsProfile profile; // todo

    public abstract Map<String, String> applyAlgorithm(RefactoringExecutionContext context);

    @Override
    protected String getTestDataPath() {
        return "testdata/src/" + getTestName(true);
    }

    private VirtualFile loadFile(String name) {
        final String fullName = getTestName(true) + "/" + name;
        return myFixture.copyFileToProject(name, fullName);
    }

    public void testMoveMethod() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateMoveMethodRefactorings);
    }

    public void testMoveField() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateMoveFieldRefactorings);
    }

    /**
     * Two methods reference each other a lot of times,
     * one o them is also referenced from another class.
     * <p>
     * Expected, that methods will be moved together
     */
    public void testMoveTogether() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateMoveTogetherRefactorings);
    }

    public void testRecursiveMethod() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateRecursiveMethodRefactorings);
    }

    public void testCrossReferencesMethods() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateMoveCrossReferencesMethodsRefactorings);
    }

    public void testReferencesOnly() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateReferencesOnlyRefactorings);
    }

    public void testCallFromNested() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateCallFromNestedRefactorings);
    }

    public void testDontMoveConstructor() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateDontMoveConstructorRefactorings);
    }

    public void testDontMoveOverridden() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateDontMoveOverriddenRefactorings);
    }

    public void testCircularDependency() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");
        final VirtualFile file3 = loadFile("ClassC.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2, file3));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateCircularDependencyRefactorings);
    }

    public void testDontMoveAbstract() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateDontMoveAbstractRefactorings);
    }

    public void testTriangularDependence() throws IOException {
        final VirtualFile file1 = loadFile("ClassA.java");
        final VirtualFile file2 = loadFile("ClassB.java");
        final VirtualFile file3 = loadFile("ClassC.java");

        final Project project = myFixture.getProject();
        final AnalysisScope analysisScope = new AnalysisScope(project, Arrays.asList(file1, file2, file3));
        profile = MetricsProfileRepository.getInstance().getProfileForName("Refactoring features");

        new RefactoringExecutionContext(project, analysisScope, profile, false
                , this::calculateTriangularDependenceRefactorings);
    }

    private void calculateMoveMethodRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(6, context.getMethodsCount());
        assertEquals(4, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertEquals("moveMethod.ClassB.methodB1()", refactorings.keySet().toArray()[0]);
        assertEquals("moveMethod.ClassA", refactorings.get("moveMethod.ClassB.methodB1()"));
    }

    private void calculateMoveFieldRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(11, context.getMethodsCount());
        assertEquals(2, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA1");
        assertContainsElements(refactorings.keySet(), "moveField.ClassA.attributeA2");
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA1"));
        assertEquals("moveField.ClassB", refactorings.get("moveField.ClassA.attributeA2"));
    }

    private void calculateMoveTogetherRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(8, context.getMethodsCount());
        assertEquals(4, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB1()");
        assertContainsElements(refactorings.keySet(), "moveTogether.ClassB.methodB2()");
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB1()"));
        assertEquals("moveTogether.ClassA", refactorings.get("moveTogether.ClassB.methodB2()"));
    }

    private void calculateRecursiveMethodRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(6, context.getMethodsCount());
        assertEquals(4, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "recursiveMethod.ClassA.methodA1()");
        assertEquals("recursiveMethod.ClassB", refactorings.get("recursiveMethod.ClassA.methodA1()"));
    }

    private void calculateMoveCrossReferencesMethodsRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(2, context.getMethodsCount());
        assertEquals(0, context.getFieldsCount());

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

    private void calculateReferencesOnlyRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(0, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(2, refactorings.size());
        assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething1()");
        assertContainsElements(refactorings.keySet(), "referencesOnly.ClassA.doSomething2()");
        assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething1()"));
        assertEquals("referencesOnly.ClassB", refactorings.get("referencesOnly.ClassA.doSomething2()"));
    }

    private void calculateCallFromNestedRefactorings(RefactoringExecutionContext context) {
        assertEquals(3, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(1, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), "callFromNested.ClassB.methodB1()");
        assertOneOf(refactorings.get("callFromNested.ClassB.methodB1()"),
                "callFromNested.ClassA", "callFromNested.ClassA.Nested");
    }

    private void calculateDontMoveConstructorRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(1, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void calculateDontMoveOverriddenRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(1, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void calculateCircularDependencyRefactorings(RefactoringExecutionContext context) {
        assertEquals(3, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(0, context.getFieldsCount());

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

    private void calculateDontMoveAbstractRefactorings(RefactoringExecutionContext context) {
        assertEquals(2, context.getClassCount());
        assertEquals(3, context.getMethodsCount());
        assertEquals(0, context.getFieldsCount());

        final Map<String, String> refactorings = applyAlgorithm(context);
        assertEquals(0, refactorings.size());
    }

    private void calculateTriangularDependenceRefactorings(RefactoringExecutionContext context) {
        assertEquals(3, context.getClassCount());
        assertEquals(6, context.getMethodsCount());
        assertEquals(0, context.getFieldsCount());

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
