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

package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.sixrr.metrics.profile.MetricsProfile;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.entity.Entity;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;
import org.ml_methods_group.utils.MetricsProfilesUtil;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.ml_methods_group.utils.RefactoringUtil.toMap;

@SuppressWarnings("WeakerAccess")
public abstract class AlgorithmAbstractTest extends LightCodeInsightFixtureTestCase {
    private static void checkStructure(@NotNull RefactoringExecutionContext context, int classes, int methods, int fields) {
        assertEquals(classes, context.getClassCount());
        assertEquals(methods, context.getMethodsCount());
        assertEquals(fields, context.getFieldsCount());
    }

    @Override
    protected String getTestDataPath() {
        return "src/test/resources/testCases/" + getTestName(true);
    }

    private String getPackageName() {
        return getTestName(true);
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

    abstract String getAlgorithmName();

    protected void checkMoveMethod(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 6, 4);

        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodB1()", getPackageName() + ".ClassA");
        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(expected, refactorings);
    }

    protected void checkCallFromNested(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 2, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), getPackageName() + ".ClassB.methodB1()");
        assertOneOf(refactorings.get(getPackageName() + ".ClassB.methodB1()"),
                getPackageName() + ".ClassA", getPackageName() + ".ClassA.Nested");
    }

    protected void checkCircularDependency(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 3, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Set<Map<String, String>> possibleRefactorings = new HashSet<>();
        final Map<String, String> possibleRefactoring1 = new HashMap<>();
        possibleRefactoring1.put(getPackageName() + ".ClassB.fooB()", getPackageName() + ".ClassA");
        possibleRefactoring1.put(getPackageName() + ".ClassC.fooC()", getPackageName() + ".ClassA");
        final Map<String, String> possibleRefactoring2 = new HashMap<>();
        possibleRefactoring2.put(getPackageName() + ".ClassA.fooA()", getPackageName() + ".ClassB");
        possibleRefactoring2.put(getPackageName() + ".ClassC.fooC()", getPackageName() + ".ClassB");
        final Map<String, String> possibleRefactoring3 = new HashMap<>();
        possibleRefactoring3.put(getPackageName() + ".ClassA.fooA()", getPackageName() + ".ClassC");
        possibleRefactoring3.put(getPackageName() + ".ClassB.fooB()", getPackageName() + ".ClassC");
        possibleRefactorings.add(possibleRefactoring1);
        possibleRefactorings.add(possibleRefactoring2);
        possibleRefactorings.add(possibleRefactoring3);
        assertTrue(possibleRefactorings.contains(refactorings));
    }

    protected void checkCrossReferencesMethods(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Set<Map<String, String>> possibleRefactorings = new HashSet<>();
        final Map<String, String> possibleRefactoring1 = new HashMap<>();
        possibleRefactoring1.put(getPackageName() + ".ClassB.methodB1()", getPackageName() + ".ClassA");
        final Map<String, String> possibleRefactoring2 = new HashMap<>();
        possibleRefactoring2.put(getPackageName() + ".ClassA.methodA1()", getPackageName() + ".ClassB");
        possibleRefactorings.add(possibleRefactoring1);
        possibleRefactorings.add(possibleRefactoring2);
        assertTrue(possibleRefactorings.contains(refactorings));
    }

    // TODO: doubts what is correct.
    protected void checkDontMoveAbstract(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    protected void checkDontMoveConstructor(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    protected void checkDontMoveOverridden(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    protected void checkMoveField(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 11, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.attributeA1", getPackageName() + ".ClassB");
        expected.put(getPackageName() + ".ClassA.attributeA2", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    protected void checkMoveTogether(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 8, 4);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodB1()", getPackageName() + ".ClassA");
        expected.put(getPackageName() + ".ClassB.methodB2()", getPackageName() + ".ClassA");
        assertEquals(expected, refactorings);
    }

    protected void checkPriority(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 9, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.methodA1()", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    protected void checkRecursiveMethod(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 5, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.methodA1()", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    protected void checkReferencesOnly(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Set<Map<String, String>> possibleRefactorings = new HashSet<>();
        final Map<String, String> possibleRefactoring1 = new HashMap<>();
        possibleRefactoring1.put(getPackageName() + ".ClassA.doSomething1()", getPackageName() + ".ClassB");
        possibleRefactoring1.put(getPackageName() + ".ClassA.doSomething2()", getPackageName() + ".ClassB");
        final Map<String, String> possibleRefactoring2 = new HashMap<>();
        possibleRefactoring2.put(getPackageName() + ".ClassB.mainF()", getPackageName() + ".ClassA");
        possibleRefactorings.add(possibleRefactoring1);
        possibleRefactorings.add(possibleRefactoring2);
        assertTrue(possibleRefactorings.contains(refactorings));
    }

    protected void checkTriangularDependence(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 8, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodToMove()", getPackageName() + ".ClassA");
        expected.put(getPackageName() + ".ClassC.methodToMove()", getPackageName() + ".ClassA");
        assertEquals(expected, refactorings);
    }

    protected void checkMobilePhoneNoFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 5, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    protected void checkMobilePhoneWithFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".Customer.getMobilePhoneNumber()", getPackageName() + ".Phone");
        assertEquals(expected, refactorings);
    }

    protected void checkMovieRentalStoreNoFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 9);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    protected void checkMovieRentalStoreWithFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 9);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".Customer.getMovie(Movie)", getPackageName() + ".Rental");
        assertEquals(expected, refactorings);
    }

    protected void checkCallFromLambda(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        final Set<Map<String, String>> possibleRefactorings = new HashSet<>();
        final Map<String, String> possibleRefactoring1 = new HashMap<>();
        possibleRefactoring1.put(getPackageName() + ".ClassA.doSomething1()", getPackageName() + ".ClassB");
        possibleRefactoring1.put(getPackageName() + ".ClassA.doSomething2()", getPackageName() + ".ClassB");
        final Map<String, String> possibleRefactoring2 = new HashMap<>();
        possibleRefactoring2.put(getPackageName() + ".ClassB.mainF()", getPackageName() + ".ClassA");
        possibleRefactorings.add(possibleRefactoring1);
        possibleRefactorings.add(possibleRefactoring2);
        assertTrue(possibleRefactorings.contains(refactorings));
    }

    protected void checkStaticFactoryMethods(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 5, 5);

        final Map<String, String> refactorings = toMap(context.getResultForName(getAlgorithmName()).getRefactorings());
        assertEquals(0, refactorings.size());
    }
}
