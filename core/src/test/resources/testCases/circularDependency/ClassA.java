package circularDependency;

public class ClassA {

    static void fooA() {
        ClassB.fooB();
        ClassB.fooB();
        ClassB.fooB();
        ClassB.fooB();
        ClassB.fooB();
        ClassB.fooB();
        ClassB.fooB();
    }
}
