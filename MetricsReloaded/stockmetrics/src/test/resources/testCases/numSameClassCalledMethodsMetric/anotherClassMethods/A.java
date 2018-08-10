public class A {
    void method1() {
        B b = new B();

        b.method1();
        b.method2();
        b.method2();
    }
}

class B {
    void method1() {
    }

    void method2() {
    }

    void method3() {
    }
}
