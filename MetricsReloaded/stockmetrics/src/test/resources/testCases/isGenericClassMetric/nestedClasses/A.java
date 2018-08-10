public class A<K, V> {
    K a;
    V b;

    K notGeneric() {
        return a;
    }

    <R> V generic(R c) {
        R x;
        return b;
    }

    public class B<Q> {

    }

    public static class C<Q, D> {
        D a;
        Q b;
        public class E<D> {
            D r;
        }
    }

    private class D {

    }

    int method(int r) {
        return r + 10;
    }
}