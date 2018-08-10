import java.util.ArrayList;
import java.util.List;

public class A {
    public int a;
    static private double b;
    static protected List<Integer> c;
    int d;

    public int aMethod() {
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
        public int r;
        static private double q;
        public int e;
    }

    public static class C {
        static public int u;
        static protected double i;
        int p;
        static public int x;
        public int o;
    }
}