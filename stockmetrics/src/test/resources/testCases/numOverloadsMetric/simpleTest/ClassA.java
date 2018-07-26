package numOverloadsMetric.simpleTest;

public class ClassA {
    static int attributeA1;
    static int attributeA2;

    void methodA1() {
        attributeA1 = 0;
        methodA2();
    }

    void methodA1(int a) {
        attributeA1 = 0;
        methodA2();
    }

    void methodA1(double b) {
        attributeA1 = 0;
        methodA2();
    }

    void methodA2() {
        attributeA2 = 0;
        attributeA1 = 0;
    }

    void methodA3(int a, double b) {
        attributeA1 = 0;
        attributeA2 = 0;
        methodA1();
        methodA2();
    }

    void methodA3(double b, int a) {
        attributeA1 = 0;
        attributeA2 = 0;
        methodA1();
        methodA2();
    }
}
