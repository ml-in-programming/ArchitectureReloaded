package anotherObjectCall;

public class A {
    void method1() {
        A a = new A();
        a.method2();
    }

    void method2() {
    }
}