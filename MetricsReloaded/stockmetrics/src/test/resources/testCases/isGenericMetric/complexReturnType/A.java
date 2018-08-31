public class A {
    Collection<java.lang.reflect.Type<?>> method(int r) {
        return new LinkedList();
    }

    <R> java.util.Collection<Type<?>> method(R r) {
        return new LinkedList();
    }

    <R> java.util.Collection<java.lang.reflect.Type<?>> method() {
        return new LinkedList();
    }
}