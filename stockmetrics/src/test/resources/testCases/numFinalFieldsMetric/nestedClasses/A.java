import java.util.ArrayList;
import java.util.List;

public class A {
    final public int a = 0;
    final static private double b = 0;
    static protected List<Integer> c;
    int d;

    final public int aMethod() {
        return 0;
    }

    private double bMethod() {
        return 0.0;
    }

    static protected List cMethod() {
        return new ArrayList();
    }

    int dMethod() {
        return 1;
    }

    public class B {
        final public int r = 0;
        private double q;
        public int e;
    }

    public static class C {
        final static public int u = 0;
        final static protected double i = 0;
        final int p = 0;
        static public int x;
        public int o;
    }
}