package staticFactoryMethodsWeak;

import java.util.ArrayList;

class Cat {
    private final Color color;
    private int weight;

    public Cat(Color color, int weight) {
        this.color = color;
        this.weight = weight;
    }

    public ArrayList<Cat> bearChildren() {
        ArrayList<Cat> children = new ArrayList<>();
        children.add(new Cat(Color.makeFromHex(0), 10));
        children.add(new Cat(Color.makeFromRGB("101010"), 20));
        children.add(new Cat(Color.makeFromPalette(20, 20, 20), 30));
        children.forEach(this::feedChild);
        children.forEach(this::feedChild);
        children.forEach(this::feedChild);
        feedChild(children.get(0));
        feedChild(children.get(1));
        feedChild(children.get(2));
        return children;
    }

    public void feedChild(Cat cat) {
        cat.weight++;
    }
}
