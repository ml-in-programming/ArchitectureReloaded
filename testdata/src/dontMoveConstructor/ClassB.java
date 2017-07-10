package dontMoveConstructor;

public class ClassB {
    static void methodB1() {
        ClassA a = new ClassA();
        ClassA b = new ClassA();
        ClassA c = new ClassA();
        ClassA d = new ClassA();
        a.equals(b);
        c.equals(d);
    }

    static void methodB2() {
        ClassA a = new ClassA();
        ClassA b = new ClassA();
        ClassA c = new ClassA();
        ClassA d = new ClassA();
        a.equals(b);
        c.equals(d);
    }
}
