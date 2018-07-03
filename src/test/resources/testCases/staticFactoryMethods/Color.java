package staticFactoryMethods;

class Color {
    private final int hex;

    static Color makeFromRGB(String rgb) {
        return new Color(Integer.parseInt(rgb, 16));
    }

    static Color makeFromPalette(int red, int green, int blue) {
        return new Color(red << 16 + green << 8 + blue);
    }

    static Color makeFromHex(int h) {
        return new Color(h);
    }

    private Color(int h) {
        return new Color(h);
    }
}
