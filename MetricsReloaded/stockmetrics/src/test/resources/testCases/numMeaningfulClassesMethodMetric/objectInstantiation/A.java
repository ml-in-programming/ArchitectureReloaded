class A {
    void method1() {
        new B();
    }

    void method2() {
        new B();
        new B();
    }

    void method3() {
        new B();
        new C();
    }
}

class B {
}

class C {
}