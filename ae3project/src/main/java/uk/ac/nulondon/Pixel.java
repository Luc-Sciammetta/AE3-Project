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

    /**
     *  method to find the brightness of a pixel
     * @return the brightness of pixel
     */
    public double brightness() {
        //TODO: implement brightness calculation
        double red = color.getRed();
        double green = getGreen();
        double blue = color.getBlue();

       return (red + green + blue) / 3.0;

     // class notes:  return(color.getRed() + color.getGreen() + color.getBlue()) / 3;

    }

    public double getGreen() {
        return color.getGreen();
    }
}
