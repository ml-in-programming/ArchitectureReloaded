package triangularDependence;

public class ClassC {

    static void methodToMove() {
        ClassA.method();
        ClassA.method();
        ClassA.method();
        ClassA.method();
        ClassB.methodToMove();
        ClassB.methodToMove();
        ClassB.methodToMove();
        ClassB.methodToMove();
    }
}
