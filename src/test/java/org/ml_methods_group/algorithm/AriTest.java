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

public class AriTest extends AlgorithmAbstractTest {
    private static final String algorithmName = "ARI";

    @Override
    String getAlgorithmName() {
        return algorithmName;
    }

    public void testMoveMethod() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveMethod).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testCallFromNested() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkCallFromNested).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testCircularDependency() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        createContext(scope, algorithmName, this::checkCircularDependency).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testCrossReferencesMethods() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkCrossReferencesMethods).executeSynchronously();
    }

    public void testDontMoveAbstract() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkDontMoveAbstract).executeSynchronously();
    }

    public void testDontMoveConstructor() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkDontMoveConstructor).executeSynchronously();
    }

    public void testDontMoveOverridden() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkDontMoveOverridden).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testMoveField() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveField).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testMoveTogether() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveTogether).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testPriority() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkPriority).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testRecursiveMethod() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkRecursiveMethod).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testReferencesOnly() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkReferencesOnly).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testTriangularDependence() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        createContext(scope, algorithmName, this::checkTriangularDependence).executeSynchronously();
    }

    public void testMobilePhoneNoFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneNoFeatureEnvy).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testMobilePhoneWithFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneWithFeatureEnvy).executeSynchronously();
    }

    public void testMovieRentalStoreNoFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Movie.java", "Rental.java");
        createContext(scope, algorithmName, this::checkMovieRentalStoreNoFeatureEnvy).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testMovieRentalStoreWithFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Movie.java", "Rental.java");
        createContext(scope, algorithmName, this::checkMovieRentalStoreWithFeatureEnvy).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testCallFromLambda() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkCallFromLambda).executeSynchronously();
    }

    // TODO: Not currently supported
    public void failing_testStaticFactoryMethods() {
        AnalysisScope scope = createScope("Cat.java", "Color.java", "Dog.java");
        createContext(scope, algorithmName, this::checkStaticFactoryMethods).executeSynchronously();
    }
}
