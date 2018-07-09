package callFromLambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassB {
    public void mainF() {
        int n = 100;
        ArrayList<Integer> arrayRandom = new ArrayList<Integer>(n);

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        for (int i=0; i < n; i++)
        {
            Integer r = rand.nextInt();
            arrayRandom.add(r);
        }

        List<Object> list = arrayRandom.stream().
                filter(integer -> ClassA.doSomething2(integer) <= integer && integer <= ClassA.doSomething1()).
                collect(Collectors.toList());
        Function<Object, Integer> sumFunction = o -> ClassA.doSomething1() + ClassA.doSomething2(o);
        int sum = sumFunction.apply(22);
        sumFunction = o -> ClassA.doSomething1() + ClassA.doSomething2(o);
        sumFunction = o -> ClassA.doSomething1() + ClassA.doSomething2(o);
        sumFunction = o -> ClassA.doSomething1() + ClassA.doSomething2(o);
    }

    static void foo() {}
}
