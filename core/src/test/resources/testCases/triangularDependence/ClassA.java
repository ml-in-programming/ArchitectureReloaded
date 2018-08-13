package triangularDependence;

public class ClassA {
    static void method() {
        Integer.parseInt("9");
    }

    void foo1() {
        method();
        foo3();
    }

    void foo2() {
        method();
        foo1();
    }

    void foo3() {
        method();
        foo2();
    }
}
