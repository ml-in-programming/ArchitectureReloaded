import java.util.List;

public class A {
    public List<Double> getListField() {
        return listField;
    }

    private List<Double> listField;

    public void setNotSetterBecauseOf0Params() {
    }

    public void setNotSetterBecauseOfMoreThan1Params(int a, int b) {
    }

    public void setListField(List<Double> listField) {
        this.listField = listField;
    }

    public class B {
        public void setA(int a) {
            this.a = a;
        }

        public void setNotSetterBecauseOfMoreThan1Params(int a, int b) {
        }

        public int getA() {
            return a;
        }

        int a;
    }

    public static class C {
        public void setA(int a) {
            this.a = a;
        }

        public void setNotSetterBecauseOfMoreThan1Params(int a, int b) {
        }

        public int getA() {
            return a;
        }

        int a;
    }
}