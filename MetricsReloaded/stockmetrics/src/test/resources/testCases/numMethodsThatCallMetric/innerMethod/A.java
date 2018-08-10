interface I {
    void method();
}

class A {
    void method1() {
        new I() {
            public void method() {
                method2();
            }
        };
    }

    public void method2() {
    }
}
