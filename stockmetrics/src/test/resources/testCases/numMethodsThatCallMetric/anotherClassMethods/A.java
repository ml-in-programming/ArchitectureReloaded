class A {
    public void method1() {
    }
}

class B {
    void method1() {
        new A().method1();
        new A().method1();
    }

    void method2() {
        new A().method1();
    }

    void method3() {
    }
}
