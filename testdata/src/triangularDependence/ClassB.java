package triangularDependence;

public class ClassB {

    static void methodToMove() {
        ClassA.method();
        ClassA.method();
        ClassA.method();
        ClassA.method();
        ClassC.methodToMove();
        ClassC.methodToMove();
        ClassC.methodToMove();
        ClassC.methodToMove();
    }

    static void foo() {}
}
