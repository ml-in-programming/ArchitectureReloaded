import java.util.List;

public class A<R extends Integer> {
    public <R extends Integer> void method(List<? super R> r) {
    }
}