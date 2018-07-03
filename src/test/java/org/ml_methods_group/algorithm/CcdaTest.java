package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;

public class CcdaTest extends AlgorithmAbstractTest {
    private static final String algorithmName = "CCDA";

    @Override
    String getAlgorithmName() {
        return algorithmName;
    }

    public void testMoveMethod() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveMethod).executeSynchronously();
    }

    public void testCallFromNested() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkCallFromNested).executeSynchronously();
    }

    public void testCircularDependency() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        createContext(scope, algorithmName, this::checkCircularDependency).executeSynchronously();
    }

    public void testCrossReferencesMethods() {
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

    // TODO: test fails, but is correct
    public void failing_testDontMoveOverridden() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkDontMoveOverridden).executeSynchronously();
    }

    public void testMoveField() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveField).executeSynchronously();
    }

    public void testMoveTogether() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveTogether).executeSynchronously();
    }

    public void testPriority() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkPriority).executeSynchronously();
    }

    public void testRecursiveMethod() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkRecursiveMethod).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testReferencesOnly() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkReferencesOnly).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testTriangularDependence() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        createContext(scope, algorithmName, this::checkTriangularDependence).executeSynchronously();
    }

    public void testMobilePhoneNoFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneNoFeatureEnvy).executeSynchronously();
    }

    // TODO: test fails, but is correct. Not fails if no search for field refactorings.
    public void failing_testMobilePhoneWithFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneWithFeatureEnvy).executeSynchronously();
    }

    public void testMovieRentalStoreNoFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Movie.java", "Rental.java");
        createContext(scope, algorithmName, this::checkMovieRentalStoreNoFeatureEnvy).executeSynchronously();
    }

    public void testMovieRentalStoreWithFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Movie.java", "Rental.java");
        createContext(scope, algorithmName, this::checkMovieRentalStoreWithFeatureEnvy).executeSynchronously();
    }
}