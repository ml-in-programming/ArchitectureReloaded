package moveField;

public class ClassB {
    static void methodB1() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB2() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB3() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB4() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB5() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB6() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        foo2();
    }

    static void methodB7() {
        ClassA.attributeA1 = 0;
        ClassA.attributeA2 = 0;
        methodB1();
        methodB2();
        foo2();
    }

    static void foo1() {
        methodB1();
        methodB2();
        methodB3();
        methodB4();
        methodB5();
        methodB6();
        methodB7();
    }

    static void foo2() {
        methodB1();
        methodB2();
        methodB3();
        methodB4();
        methodB5();
        methodB6();
        methodB7();
    }
}
