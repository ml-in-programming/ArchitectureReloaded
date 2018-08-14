package recursiveMethod;

public class ClassA {

    static void methodA1() {
        methodA1();
        methodA1();
        methodA1();
        methodA1();
        methodA1();
        methodA1();
        ClassB.methodB2();
        ClassB.methodB2();
        ClassB.methodB2();
        ClassB.methodB2();
    }

    static void foo() {}
}
