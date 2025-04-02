package uk.ac.nulondon;

import java.awt.*;

public class Pixel {
    Pixel left;
    Pixel right;

    double energy;

    final Color color;

    public Pixel(int rgb) {
        this.color = new Color(rgb);
    }

    public Pixel(Color color) {
        this.color = color;
    }

    // method to find the brightness of a pixel
    public double brightness() {
        double red = color.getRed();
        double green = getGreen();
        double blue = color.getBlue();

       return (red + green + blue) / 3.0;
    }

    public double getGreen() {
        return color.getGreen();
    }
}
