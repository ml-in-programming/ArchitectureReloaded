package numOverloadsMetric.testSupers;

public class ClassB extends ClassA {
    @Override
    void methodA1() {
        int a = 0;
    }

    void methodA1(double b) {
        int a = 2;
    }
}
