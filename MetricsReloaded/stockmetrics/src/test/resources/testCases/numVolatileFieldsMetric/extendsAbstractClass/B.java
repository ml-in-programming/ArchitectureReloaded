import java.util.ArrayList;
import java.util.List;

public class B extends A {
    final static public int Bfield1;
    volatile private int Bfield2;
    final protected int Bfield3;
    static int y;

    @Override
    protected List cMethod() {
        return new ArrayList();
    }
}