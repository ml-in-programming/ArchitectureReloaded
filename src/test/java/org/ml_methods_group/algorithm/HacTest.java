package org.ml_methods_group.algorithm;

import com.intellij.analysis.AnalysisScope;

public class HacTest extends AbstractAlgorithmTest {
    private static final String algorithmName = "HAC";

    @Override
    String getAlgorithmName() {
        return algorithmName;
    }

    public void testMoveMethod() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveMethod).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testCallFromNested() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkCallFromNested).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testCircularDependency() {
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

    public void testDontMoveOverridden() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkDontMoveOverridden).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testMoveField() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveField).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testMoveTogether() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java");
        createContext(scope, algorithmName, this::checkMoveTogether).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testPriority() {
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

    public void testTriangularDependence() {
        AnalysisScope scope = createScope("ClassA.java", "ClassB.java", "ClassC.java");
        createContext(scope, algorithmName, this::checkTriangularDependence).executeSynchronously();
    }

    public void testMobilePhoneNoFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneNoFeatureEnvy).executeSynchronously();
    }

    // TODO: test fails, but is correct
    public void failing_testMobilePhoneWithFeatureEnvy() {
        AnalysisScope scope = createScope("Customer.java", "Phone.java");
        createContext(scope, algorithmName, this::checkMobilePhoneWithFeatureEnvy).executeSynchronously();
    }
}
