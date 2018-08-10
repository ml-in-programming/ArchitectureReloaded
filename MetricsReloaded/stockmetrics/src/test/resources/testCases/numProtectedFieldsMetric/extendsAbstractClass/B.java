import java.util.ArrayList;
import java.util.List;

public class B extends A {
    public int Bfield1;
    private int Bfield2;
    protected int Bfield3;

    @Override
    protected List cMethod() {
        return new ArrayList();
    }
}