package numOverloadsMetric.testSupers;


public class ClassD extends ClassC {
    @Override
    void methodA1(int a) {
        int b = 0;
    }

    @Override
    void methodA1() {
        int b = 0;
    }

    void methodA1(int a, double b) {
        a++;
        b += 1;
    }
}