package circularDependency;

public class ClassC {

    static void fooC() {
        ClassA.fooA();
        ClassA.fooA();
        ClassA.fooA();
        ClassA.fooA();
        ClassA.fooA();
        ClassA.fooA();
    }
}
