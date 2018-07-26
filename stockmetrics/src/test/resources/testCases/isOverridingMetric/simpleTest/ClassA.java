package isOverridingMetric.simpleTest;

import org.jetbrains.annotations.NotNull;

public class ClassA extends ClassB {
    void methodA1() {
    }

    @Override
    protected void methodA2() {
        int a = 2;
    }
}