package org.jetbrains.research.groups.ml_methods.algorithm;

public class CcdaTest extends AlgorithmAbstractTest {
    private static final Algorithm algorithm = new CCDA();

    public void testMoveMethod() {
        executeTest(testCasesChecker::checkMoveMethod, "ClassA.java", "ClassB.java");
    }

    public void testCallFromNested() {
        executeTest(testCasesChecker::checkCallFromNested, "ClassA.java", "ClassB.java");
    }

    public void testCircularDependency() {
        executeTest(testCasesChecker::checkCircularDependency, "ClassA.java", "ClassB.java", "ClassC.java");
    }

    public void testCrossReferencesMethods() {
        executeTest(testCasesChecker::checkCrossReferencesMethods, "ClassA.java", "ClassB.java");
    }

    public void testDontMoveAbstract() {
        executeTest(testCasesChecker::checkDontMoveAbstract, "ClassA.java", "ClassB.java");
    }

    public void testDontMoveConstructor() {
        executeTest(testCasesChecker::checkDontMoveConstructor, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testDontMoveOverridden() {
        executeTest(testCasesChecker::checkDontMoveOverridden, "ClassA.java", "ClassB.java");
    }

    public void testMoveField() {
        executeTest(testCasesChecker::checkMoveField, "ClassA.java", "ClassB.java");
    }

    public void testMoveTogether() {
        executeTest(testCasesChecker::checkMoveTogether, "ClassA.java", "ClassB.java");
    }

    public void testPriority() {
        executeTest(testCasesChecker::checkPriority, "ClassA.java", "ClassB.java");
    }

    public void testRecursiveMethod() {
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

    public void testCallFromLambda() {
        executeTest(testCasesChecker::checkCallFromLambda, "ClassA.java", "ClassB.java");
    }

    // TODO: Not currently supported
    public void failing_testStaticFactoryMethods() {
        executeTest(testCasesChecker::checkStaticFactoryMethods, "Cat.java", "Color.java", "Dog.java");
    }

    // TODO: Not currently supported
    public void failing_testStaticFactoryMethodsWeak() {
        executeTest(testCasesChecker::checkStaticFactoryMethodsWeak, "Cat.java", "Color.java", "Dog.java");
    }

    @Override
    protected Algorithm getAlgorithm() {
        return algorithm;
    }
}