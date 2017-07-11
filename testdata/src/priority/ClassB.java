package priority;

public class ClassB {

    static void methodB1() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        methodB2();
        methodB3();
        methodB4();
    }

    static void methodB2() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        methodB2();
        methodB2();
        methodB1();
    }

    static void methodB3() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        methodB2();
        methodB1();
        methodB5();
    }

    static void methodB4() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        methodB2();
        methodB1();
        methodB5();
    }

    static void methodB5() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        methodB2();
        methodB1();
        methodB4();
    }
}
