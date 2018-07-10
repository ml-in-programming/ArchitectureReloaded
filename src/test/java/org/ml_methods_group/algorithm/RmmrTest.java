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
    public void failing_testCallFromNested() {
        executeTest(testCasesChecker::checkCallFromNested, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testCircularDependency() {
        executeTest(testCasesChecker::checkCircularDependency, "ClassA.java", "ClassB.java", "ClassC.java");
    }

    // TODO: Not currently supported
    public void failing_testCrossReferencesMethods() {
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
    public void failing_testMoveTogether() {
        executeTest(testCasesChecker::checkMoveTogether, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testPriority() {
        executeTest(testCasesChecker::checkPriority, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testRecursiveMethod() {
        executeTest(testCasesChecker::checkRecursiveMethod, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testReferencesOnly() {
        executeTest(testCasesChecker::checkReferencesOnly, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testTriangularDependence() {
        executeTest(testCasesChecker::checkTriangularDependence, "ClassA.java", "ClassB.java", "ClassC.java");
    }

    public void testMobilePhoneNoFeatureEnvy() {
        executeTest(testCasesChecker::checkMobilePhoneNoFeatureEnvy, "Customer.java", "Phone.java");
    }

    // TODO: Not currently supported
    public void failing_testMobilePhoneWithFeatureEnvy() {
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
     Failure explanation: almost all words appear in both classes that is why idf is 0.
     As a result vector is something like that: 3, 0, 0, ..., 0.
     And there is no intersection with not nulls so context similarity is 0.
     getMobilePhone method has big distance (almost 1) with its class and big dissimilarity with Phone class.
     But own class (Customer) wins...
      */
    public void failing_testCallFromLambda() {
        executeTest(testCasesChecker::checkCallFromLambda, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testStaticFactoryMethods() {
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