package moveTogether;

public class ClassA {
    static int attributeA1;
    static int attributeA2;

    static void methodA1() {
        attributeA1 = 0;
        methodA2();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
    }

    static void methodA2() {
        attributeA2 = 0;
        attributeA1 = 0;
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
    }

    static void methodA3() {
        attributeA1 = 0;
        attributeA2 = 0;
        methodA1();
        methodA2();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
    }

    static void methodA4() {
        attributeA2 = 0;
        attributeA1 = 0;
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
    }

    static void methodA5() {
        attributeA1 = 0;
        attributeA2 = 0;
        methodA1();
        methodA2();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
        ClassB.methodB1();
    }
}
