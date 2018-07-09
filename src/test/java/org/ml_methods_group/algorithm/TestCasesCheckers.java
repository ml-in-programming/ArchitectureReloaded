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

import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.refactoring.RefactoringExecutionContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.intellij.testFramework.UsefulTestCase.assertContainsElements;
import static com.intellij.testFramework.UsefulTestCase.assertOneOf;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.ml_methods_group.utils.RefactoringUtil.toMap;

class TestCasesCheckers {
    private static final String CHECK_METHODS_PREFIX = "check";
    private final String algorithmName;

    TestCasesCheckers(String algorithmName) {
        this.algorithmName = algorithmName;
    }

    @NotNull
    private static String getPackageName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String packageName = stackTrace[2].getMethodName().substring(CHECK_METHODS_PREFIX.length());
        return packageName.substring(0, 1).toLowerCase() + packageName.substring(1);
    }

    private static void checkStructure(@NotNull RefactoringExecutionContext context, int classes, int methods, int fields) {
        assertEquals(classes, context.getClassCount());
        assertEquals(methods, context.getMethodsCount());
        assertEquals(fields, context.getFieldsCount());
    }

    void checkMoveMethod(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 6, 4);

        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodB1()", getPackageName() + ".ClassA");
        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(expected, refactorings);
    }

    void checkCallFromNested(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 2, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(1, refactorings.size());
        assertContainsElements(refactorings.keySet(), getPackageName() + ".ClassB.methodB1()");
        assertOneOf(refactorings.get(getPackageName() + ".ClassB.methodB1()"),
                getPackageName() + ".ClassA", getPackageName() + ".ClassA.Nested");
    }

    void checkCircularDependency(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 3, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
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

    void checkCrossReferencesMethods(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
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
    void checkDontMoveAbstract(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkDontMoveConstructor(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 2, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkDontMoveOverridden(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 3, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkMoveField(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 11, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.attributeA1", getPackageName() + ".ClassB");
        expected.put(getPackageName() + ".ClassA.attributeA2", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    void checkMoveTogether(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 8, 4);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodB1()", getPackageName() + ".ClassA");
        expected.put(getPackageName() + ".ClassB.methodB2()", getPackageName() + ".ClassA");
        assertEquals(expected, refactorings);
    }

    void checkPriority(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 9, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.methodA1()", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    void checkRecursiveMethod(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 5, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassA.methodA1()", getPackageName() + ".ClassB");
        assertEquals(expected, refactorings);
    }

    void checkReferencesOnly(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
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

    void checkTriangularDependence(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 8, 0);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".ClassB.methodToMove()", getPackageName() + ".ClassA");
        expected.put(getPackageName() + ".ClassC.methodToMove()", getPackageName() + ".ClassA");
        assertEquals(expected, refactorings);
    }

    void checkMobilePhoneNoFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 5, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkMobilePhoneWithFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 2);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".Customer.getMobilePhoneNumber()", getPackageName() + ".Phone");
        assertEquals(expected, refactorings);
    }

    void checkMovieRentalStoreNoFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 9);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkMovieRentalStoreWithFeatureEnvy(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 9);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        final Map<String, String> expected = new HashMap<>();
        expected.put(getPackageName() + ".Customer.getMovie(Movie)", getPackageName() + ".Rental");
        assertEquals(expected, refactorings);
    }

    void checkCallFromLambda(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 2, 4, 1);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
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

    void checkStaticFactoryMethods(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 5);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }

    void checkStaticFactoryMethodsWeak(@NotNull RefactoringExecutionContext context) {
        checkStructure(context, 3, 7, 5);

        final Map<String, String> refactorings = toMap(context.getResultForName(algorithmName).getRefactorings());
        assertEquals(0, refactorings.size());
    }
}
