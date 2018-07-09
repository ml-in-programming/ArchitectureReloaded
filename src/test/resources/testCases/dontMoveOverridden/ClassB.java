package dontMoveOverridden;

public class ClassB {

    final ClassA a = new ClassA();

    void methodB1() {
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
    }

    void methodB2() {
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
        a.compare(0, 1);
    }
}
