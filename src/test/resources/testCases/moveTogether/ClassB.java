package moveTogether;

public class ClassB {
    static int attributeB1;
    static int attributeB2;

    static void methodB1() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();

        ClassA.methodA2();
        ClassA.methodA2();
        ClassA.methodA2();
        ClassA.methodA2();
        methodB2();
        methodB2();
        methodB2();
        methodB2();
        methodB2();
        methodB2();
        methodB2();
        methodB2();
    }

    static void methodB2() {
        methodB1();
        methodB1();
        methodB1();
        methodB1();
        methodB1();
        methodB1();
        methodB1();
        methodB1();
    }

    static void methodB3() {
        attributeB1 = 0;
        attributeB1 = 0;
    }
}
