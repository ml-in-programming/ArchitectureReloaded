package callFromLambda;

public class ClassA {
    private final static int CONST = 19;

    static int doSomething1() {
        return 25 + CONST;
    }

    static int doSomething2(Object o) {
        return 21;
    }
}
