class A {
    int i;

    B b;

    C c;

    void method1() {
        i = 0;
    }

    void method2() {
        b = null;
    }

    void method3() {
        b = null;
        b = null;
    }

    void method4() {
        b = null;
        c = null;
    }
}

class B {
}

class C {
}
