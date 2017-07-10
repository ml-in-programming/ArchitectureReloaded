package crossReferencesMethods;

public class ClassB {
    static void methodB1() {
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
        ClassA.methodA1();
    }
}
