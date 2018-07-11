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

public class RmmrTest extends AlgorithmAbstractTest {
    private static final String algorithmName = "RMMR";
    private static final TestCasesCheckers testCasesChecker = new TestCasesCheckers(algorithmName, false);

    public void testMoveMethod() {
        executeTest(testCasesChecker::checkMoveMethod, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: In terms of contextual similarity methodB1 has highest score with its own class,
    and lower with Nested class. methodB1 has no body, so it's conceptual set is empty, that is why conceptual similarity
    is very low, but if we add its own class (ClassB) to conceptual set of methodB1, then methodB1 -> Nested refactoring
    will be suggested. But it will fail a lot of tests that pass now (only 6 will pass), so it is bad decision to
    include its own class to method's conceptual set.
     */
    public void testCallFromNested() {
        executeTest(testCasesChecker::checkCallFromNested, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: because we include statistic of processing method to class statistic it reflects
    on dot product of method's and class's vectors. In this test case similarity is very high because of that
    so algorithm doesn't find any refactorings. Dependency sets intersection is always empty so it doesn't affect the results.
     */
    public void testCircularDependency() {
        executeTest(testCasesChecker::checkCircularDependency, "ClassA.java", "ClassB.java", "ClassC.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: interesting case because all methods with all classes have max distances - 1.
    Contextual distance is max because all words appear everywhere, conceptual because intersection is empty.\
    Consider: if we have big distance with source than maybe suggest a refactoring? (big doubts)
     */
    public void testCrossReferencesMethods() {
        executeTest(testCasesChecker::checkCrossReferencesMethods, "ClassA.java", "ClassB.java");
    }

    public void testDontMoveAbstract() {
        executeTest(testCasesChecker::checkDontMoveAbstract, "ClassA.java", "ClassB.java");
    }

    public void testDontMoveConstructor() {
        executeTest(testCasesChecker::checkDontMoveConstructor, "ClassA.java", "ClassB.java");
    }

    public void testDontMoveOverridden() {
        executeTest(testCasesChecker::checkDontMoveOverridden, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testMoveField() {
        executeTest(testCasesChecker::checkMoveField, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: methodB1 has conceptual distance 0 with ClassA, but contextual similarity distance is very high.
    Meanwhile total distance with ClassB of methods methodB1 and methodB2 is less than 0.2 which is the least.
    Again there are a lot of 0 in vectors because of appearance in both classes. That problem must disappear on big projects.
     */
    public void testMoveTogether() {
        executeTest(testCasesChecker::checkMoveTogether, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: for methodA1 all distances are 1 because dependency set is empty,
     and "a1" and "method" appear in both classes so in vector they are 0 coordinates.
     Consider: problem is because it is two classes test case, otherwise we could count that a1
     appears much more often in ClassB than in ClassA and context distance would be smaller.
     */
    public void testPriority() {
        executeTest(testCasesChecker::checkPriority, "ClassA.java", "ClassB.java");
    }

    public void testRecursiveMethod() {
        executeTest(testCasesChecker::checkRecursiveMethod, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: refactoring can be found if add to methods in ClassA some references to its own class to get
    ClassA in conceptual set of this methods. Without that conceptual distance is always 1 and contextual distance
    plays a big role and it is the lowest with source classes.
    Consider: adding field attribute = "result" to ClassA solves the problem.
     */
    public void testReferencesOnly() {
        executeTest(testCasesChecker::checkReferencesOnly, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: RMMR doesn't consider global dependency and structure, so it is prospective that this test fails.
    Methods "methodToMove" from ClassB and ClassC have big distance with ClassA. Mostly because of contextual distance but
    dependency distance is high too (even weights 0.7 and 0.3 doesn't solve a problem). With other two classes, for example ClassB.methodToMove,
    has almost equal distances (about 0.5), but with it's own class distance is 0.5 because of contextual similarity,
    and with other class because of conceptual similarity.
     */
    public void testTriangularDependence() {
        executeTest(testCasesChecker::checkTriangularDependence, "ClassA.java", "ClassB.java", "ClassC.java");
    }

    public void testMobilePhoneNoFeatureEnvy() {
        executeTest(testCasesChecker::checkMobilePhoneNoFeatureEnvy, "Customer.java", "Phone.java");
    }

    // TODO: Not currently supported
    /*
     Failure explanation: almost all words appear in both classes that is why idf is 0.
     As a result vector is something like that: 3, 0, 0, ..., 0.
     And there is no intersection with not nulls so context similarity is 0.
     getMobilePhone method has big distance (almost 1) with its class and big dissimilarity with Phone class.
     But own class (Customer) wins...
      */
    public void testMobilePhoneWithFeatureEnvy() {
        executeTest(testCasesChecker::checkMobilePhoneWithFeatureEnvy, "Customer.java", "Phone.java");
    }

    public void testMovieRentalStoreNoFeatureEnvy() {
        executeTest(testCasesChecker::checkMovieRentalStoreNoFeatureEnvy, "Customer.java", "Movie.java", "Rental.java");
    }

    public void testMovieRentalStoreWithFeatureEnvy() {
        executeTest(testCasesChecker::checkMovieRentalStoreWithFeatureEnvy, "Customer.java", "Movie.java", "Rental.java");
    }

    // TODO: Not currently supported
    /*
    Failure explanation: the same problem as in references only test case.
    Consider: if add CONST to doSomething2() then test passes.
     */
    public void testCallFromLambda() {
        executeTest(testCasesChecker::checkCallFromLambda, "ClassA.java", "ClassB.java");
    }

    public void testStaticFactoryMethods() {
        executeTest(testCasesChecker::checkStaticFactoryMethods, "Cat.java", "Color.java", "Dog.java");
    }

    public void testStaticFactoryMethodsWeak() {
        executeTest(testCasesChecker::checkStaticFactoryMethodsWeak, "Cat.java", "Color.java", "Dog.java");
    }

    @Override
    protected String getAlgorithmName() {
        return algorithmName;
    }
}