package circularDependency;

public class ClassB {

    static void fooB() {
        ClassC.fooC();
        ClassC.fooC();
        ClassC.fooC();
        ClassC.fooC();
        ClassC.fooC();
        ClassC.fooC();
    }
}
