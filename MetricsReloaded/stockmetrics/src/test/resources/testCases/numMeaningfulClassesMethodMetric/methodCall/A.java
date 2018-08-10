class A {
    void method1() {
        B.method();
    }

    void method2() {
        B.method();
        B.method()
    }

    void method3() {
        B.method();
        C.method();
    }
}

class B {
    static void method() {
    }
}

class C {
    static void method() {
    }
}
