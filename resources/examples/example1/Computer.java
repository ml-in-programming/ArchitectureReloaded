public class Computer {
    Computer() {
        System.out.println("Constructor of Computer class.");
    }

    void computer_method() {
        System.out.println("Power gone! Shut down your PC soon...");
    }

    public static void main(String[] args) {
        Computer my = new Computer();
        Laptop your = new Laptop();

        my.computer_method();
        your.laptop_method();
    }
}

