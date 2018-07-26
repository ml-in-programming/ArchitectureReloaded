package numSameClassCalledMethodsMetric.sameMethodTwice;

interface I {
    void method1();

    void method2();

    void method3();
}

public class A implements I {
    void method1() {
        A a = new A();

        method1();
        a.method2();

        I i = a;
        i.method1();
    }

    void method2() {
    }

    void method3() {
    }
}
