public class A {
    void method1() {
        B b = new B();

        b.method1();
        b.method2();

        I i = b;
        i.method1();
    }
}

interface I {
    void method1();

    void method2();

    void method3();
}

class B implements I {
    void method1() {
    }

    void method2() {
    }

    void method3() {
    }
}
