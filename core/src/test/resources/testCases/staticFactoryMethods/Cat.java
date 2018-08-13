package staticFactoryMethods;

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
        return children;
    }

    public void feedChild(Cat cat) {
        cat.weight++;
    }
}
