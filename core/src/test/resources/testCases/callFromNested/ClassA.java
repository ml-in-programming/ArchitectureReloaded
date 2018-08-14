package callFromNested;

public class ClassA {

    class Nested {
        private int field;

        public Nested() {
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
        }

        void methodA1() {
            field++;
            field++;
            field++;
            field++;
            field++;
            field++;
            field++;
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
            ClassB.methodB1();
        }
    }
}
