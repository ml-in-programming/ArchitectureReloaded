package priority;

public class ClassA {
    static void methodA1() {
    }

    static void methodA2() {
        methodA1();
        methodA3();
        methodA4();
    }

    static void methodA3() {
        methodA2();
        methodA2();
    }

    static void methodA4() {
        methodA2();
        methodA3();
    }
}
