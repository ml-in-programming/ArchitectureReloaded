import java.util.ArrayList;
import java.util.List;

public class A {
    public int a;
    private double b;
    protected List<Integer> c;
    int d;

    public int aMethod() {
        return 0;
    }

    private double bMethod() {
        return 0.0;
    }

    protected List cMethod() {
        return new ArrayList();
    }

    int dMethod() {
        return 1;
    }
}