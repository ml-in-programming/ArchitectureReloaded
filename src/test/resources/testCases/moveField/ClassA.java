package moveField;

public class ClassA {

    static int attributeA1;
    static int attributeA2;

    static void methodA1() {
        methodA2();
        methodA2();
    }

    static void methodA2() {
        methodA1();
        methodA2();
    }
}
