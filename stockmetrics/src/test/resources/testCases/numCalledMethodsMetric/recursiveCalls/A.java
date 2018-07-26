package numCalledMethodsMetric.recursiveCalls;

public class A {
    void method1() {
        method1();
        method1();
    }

    void method2() {
    }
}
