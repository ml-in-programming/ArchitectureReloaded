import java.util.List;

public class A {
    private int intField;
    private List<Double> listField;

    public void setNotSetterBecauseOf0Params() {
    }

    public void setNotSetterBecauseOfMoreThan1Params(int a, int b) {
    }

    public void setIntField(int intField) {
        this.intField = intField;
    }

    public void setListField(List<Double> listField) {
        this.listField = listField;
    }
}