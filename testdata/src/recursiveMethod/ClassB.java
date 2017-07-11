package recursiveMethod;

public class ClassB {
    static int attributeB1;
    static int attributeB2;

    static void methodB1() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
    }

    static void methodB2() {
        attributeB1 = 0;
        attributeB2 = 0;
        methodB1();
        methodB1();
        methodB1();
    }

    static void methodB3() {
        attributeB1 = 0;
        methodB1();
        methodB1();
        methodB1();
        methodB2();
        methodB2();
    }
}
