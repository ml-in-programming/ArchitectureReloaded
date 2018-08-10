interface I {
    void method1();

    void method2();
}

class A implements I {
    public void method1() {
    }

    public void method2() {
    }
}

class B implements I {
    public void method1() {
    }

    public void method2() {
    }
}

class C {
    void method1() {
        I i1 = new A();
        i1.method1();

        I i2 = new B();
        i2.method2();
    }
}
