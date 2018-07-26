package numCalledMethodsMetric.sameClassMethods;

public class A {
    void method1() {
        method2();
        method3();
        method3();
    }

    void method2() {
    }

    void method3() {
    }

    void method4() {
    }
}
