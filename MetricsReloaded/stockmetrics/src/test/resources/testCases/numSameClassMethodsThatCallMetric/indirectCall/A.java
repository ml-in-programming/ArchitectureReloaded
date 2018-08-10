interface I {
    void method1();
}

class A implements I {
    public void method1() {
    }

    private void method2() {
        I i = new A();
        i.method1();
    }
}
