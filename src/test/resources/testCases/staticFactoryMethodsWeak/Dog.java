package staticFactoryMethodsWeak;

import java.util.ArrayList;

class Dog {
    private final Color color;
    private int weight;

    public Dog(Color color, int weight) {
        this.color = color;
        this.weight = weight;
    }

    public ArrayList<Dog> bearChildren() {
        ArrayList<Dog> children = new ArrayList<>();
        children.add(new Dog(Color.makeFromHex(0), 10));
        children.add(new Dog(Color.makeFromRGB("101010"), 20));
        children.add(new Dog(Color.makeFromPalette(20, 20, 20), 30));
        children.forEach(this::feedChild);
        children.forEach(this::feedChild);
        children.forEach(this::feedChild);
        feedChild(children.get(0));
        feedChild(children.get(1));
        feedChild(children.get(2));
        return children;
    }

    public void feedChild(Dog dog) {
        dog.weight++;
    }
}
