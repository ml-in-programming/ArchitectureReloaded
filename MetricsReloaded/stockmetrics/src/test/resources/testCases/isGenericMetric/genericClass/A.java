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

    int method(int r) {
        return r + 10;
    }
}