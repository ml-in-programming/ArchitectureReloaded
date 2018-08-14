package referencesOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassB {
    public void mainF() {
        List<Object> list = new ArrayList<>().stream()
                .map(ClassA::doSomething1)
                .map(ClassA::doSomething1)
                .map(ClassA::doSomething1)
                .map(ClassA::doSomething1)
                .collect(Collectors.toList());
        Function<Object, Object> myFunction = ClassA::doSomething2;
        myFunction = ClassA::doSomething2;
        myFunction = ClassA::doSomething2;
        myFunction = ClassA::doSomething2;
    }

    static void foo() {}
}
