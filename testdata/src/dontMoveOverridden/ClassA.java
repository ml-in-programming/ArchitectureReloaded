package dontMoveOverridden;

import java.util.Comparator;

public class ClassA implements Comparator<Integer> {

    @Override
    public int compare(Integer o1, Integer o2) {
        return o1.hashCode() - o2.hashCode();
    }
}
