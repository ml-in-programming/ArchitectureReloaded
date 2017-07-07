public class Computer {
    Computer() {
        System.out.println("Constructor of Computer class.");
    }

    void computerMethod() {
        System.out.println("Power gone! Shut down your PC soon...");
    }

    public static void main(String[] args) {
        Computer my = new Computer();
        Laptop your = new Laptop();

        my.computerMethod();
        your.laptopMethod();
    }
}

