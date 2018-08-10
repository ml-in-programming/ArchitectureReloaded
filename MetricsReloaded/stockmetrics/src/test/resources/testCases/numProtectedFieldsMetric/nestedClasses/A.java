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

    public class B {
        public int r;
        private double q;
        public int e;
    }

    public static class C {
        public int u;
        protected double i;
        public int x;
        public int o;
    }
}