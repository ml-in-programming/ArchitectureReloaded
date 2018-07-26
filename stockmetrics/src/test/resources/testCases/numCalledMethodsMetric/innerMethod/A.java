interface I {
    void method();
}

public class A {
    void method1() {
        new I() {
            void method() {
                method2();
            }
        }
    }

    void method2() {

    }
}
