package recursiveMethod;

public class ClassA {
    static int attributeA1;
    static int attributeA2;

    static void methodA1() {
        attributeA1 = 0;
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

    static void methodA2() {
        attributeA2 = 0;
        attributeA1 = 0;
    }

    static void methodA3() {
        attributeA1 = 0;
        attributeA2 = 0;
    }
}
