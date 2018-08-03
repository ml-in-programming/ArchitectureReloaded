package basicGenerationConstraint;
abstract public class A {
    public void move1() {
        B b = new B();
        b.b1();
        b.b2(10);
        b.b2(11);
        b.b2(12);
        notMove2();
    }

    public void move2(int n) {
        B b = new B();
        b.b2(n);
        notMove1();
    }

    void move3() {
    }

    abstract void move4();
}