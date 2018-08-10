public class A<K> {
    K a;
    void notGeneric() {
        int a = 2;
    }

    <R> R generic1(R c) {
        R x;
        return c;
    }

    <R> int generic2(R c) {
        return 10;
    }
}