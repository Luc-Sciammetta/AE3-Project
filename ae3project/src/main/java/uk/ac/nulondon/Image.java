package uk.ac.nulondon;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Function;


public class Image {
    private final List<Pixel> rows;

    private int width;
    private int height;


    public Image(BufferedImage img) {
        width = img.getWidth();
        height = img.getHeight();
        rows = new ArrayList<>();
        Pixel current = null;
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Pixel pixel = new Pixel(img.getRGB(col, row));
                if (col == 0) {
                    rows.add(pixel);
                } else {
                    current.right = pixel;
                    pixel.left = current;
                }
                current = pixel;
            }
        }
    }

    public BufferedImage toBufferedImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int row = 0; row < height; row++) {
            Pixel pixel = rows.get(row);
            int col = 0;
            while (pixel != null) {
                image.setRGB(col++, row, pixel.color.getRGB());
                pixel = pixel.right;
            }
        }
        return image;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Calculates the energy for the specific pixel
     * @param above the above pixel
     * @param current the current pixel
     * @param below the below pixel
     * @return the calculate energy double
     */
    double energy(Pixel above, Pixel current, Pixel below) {
        //TODO: Calculate energy based on neighbours of the current pixel
        if(above == null || below == null || current.left == null || current.right == null) {
            return current.brightness();
        }

        double a = above.left.brightness();
        double b = above.brightness();
        double c = above.right.brightness();

        double d = current.left.brightness();
        double f = current.right.brightness();

        double g = below.left.brightness();
        double h = below.brightness();
        double i = below.right.brightness();

        // find the horizontal energy
        double HorizEnergy = (a + (2 * (d)) + g) - (c + (2 * (f)) + i);
        // find the vertical energy
        double VertEnergy = (a + (2 * (b)) + c) - (g + (2 * (h)) + i);
        // find and return the energy
        return Math.sqrt(Math.pow(2, HorizEnergy) + Math.pow(2, VertEnergy));

        // find the horizontal energy
//      double HorizEnergy = (above.left.brightness() + (2 * (current.left.brightness())) + below.left.brightness()) -
//              (above.right.brightness() + (2 * (current.right.brightness())) + below.right.brightness());
//        // find the vertical energy
//      double VertEnergy = (above.left.brightness() + (2 * (above.brightness())) + above.right.brightness()) -
//              (below.left.brightness() + (2 * (below.brightness())) + below.right.brightness());
//        // find and return the energy
//      return Math.sqrt(Math.pow(2, HorizEnergy) + Math.pow(2, VertEnergy));
    }

    /**
     * calculates the energy property of each pixel
     */
    public void calculateEnergy() {
        //TODO: calculate energy for all the pixels in the image
        for (int row = 0; row < rows.size(); row++){
            int numRights = 0;
            Pixel current = rows.get(row);
            while (current != null){
                if (row == 0 || row >= rows.size() - 1 || numRights == 0 || current.right == null){ //top, bottom, or sides of the image
                    current.energy = current.brightness();
                }else{
                    Pixel above = rows.get(row - 1);
                    Pixel below = rows.get(row + 1);
                    for (int i = 0; i < numRights; i++){
                        above = above.right;
                        below = below.right;
                    }
                    current.energy = energy(above, current, below);
                }
                current = current.right;
                numRights += 1;
            }
        }
    }

    /**
     *  Highlights a seam in the image
     * @param seam given seam to hightlights
     * @param color given color to highlight the seam
     * @return the original seam
     */
    public List<Pixel> higlightSeam(List<Pixel> seam, Color color) {
        //TODO: highlight the seam, return previous values
        List<Pixel> originalSeam = new ArrayList<>(); //deep copy here
        for (Pixel pixel : seam) {
            originalSeam.add(new Pixel(pixel.color));
        }

        int row = height - 1;
        for (Pixel pixel : seam) {
            Pixel newPixel = new Pixel(color);
            if (pixel.left != null) {
                pixel.left.right = newPixel;
                newPixel.left = pixel.left;
            } else {
                // left most pixel
                rows.set(row, newPixel);
            }
            if (pixel.right != null) {
                pixel.right.left = newPixel;
                newPixel.right = pixel.right;
            }
            row--;
        }
        return originalSeam;
    }

    /**
     *
     * @param seam
     */
    public void removeSeam(List<Pixel> seam) {
        //TODO: remove the provided seam
        width--;
        int row = height - 1;
        for(Pixel pixel : seam){
            if(pixel.left != null){
                pixel.left.right = pixel.right; // jump the pixel from the left
            } else {
                // left most pixel
                rows.set(row, pixel.right);
            }
            if(pixel.right != null){
                pixel.right.left = pixel.left; // jump the pixel from the right
            }
            row--;
        }
    }

    public void addSeam(List<Pixel> seam) {
        //TODO: Add the provided seam
        width++;

        for (int row = 0; row < seam.size(); row++) {
            Pixel seamPixel = seam.get(row);
            Pixel rowHead = rows.get(row);

            // if seamPixel should be inserted at the beginning
            if(seamPixel.left == null){
                seamPixel.right = rowHead;
                if(rowHead != null) rowHead.left = seamPixel;
                rows.set(row, seamPixel);
            } else{
                Pixel current = seamPixel.left.right;

                // Insert seamPixel in between left and right
                seamPixel.left.right = seamPixel;
                seamPixel.right = current;
                if (current != null) current.left = seamPixel;
            }
        }
    }

    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        //finds the seam with the highest energy and then returns it

        //TODO: find the seam which maximizes total value extracted from the given pixel
        // Arrays to store the cumulative max values of seams for the previous and current row
        double[] previousValues = new double[width];
        double[] currentValues = new double[width];

        // Lists to store the seam paths for the previous and current row
        List<List<Pixel>> previousSeams = new ArrayList<>();
        List<List<Pixel>> currentSeams = new ArrayList<>();


        // Start processing from the first row
        Pixel currentPixel = rows.getFirst();
        int col = 0;
        // Initialize the first row values and corresponding seams
        while (currentPixel != null){
            previousValues[col] = valueGetter.apply(currentPixel);
            previousSeams.add(List.of(currentPixel));
            currentPixel = currentPixel.right;
            col++;
        }

        // Process all rows to compute the max-value seams
        for(int row = 1; row < height; row++){
            currentPixel = rows.get(row); // get the first pixel of the current row
            col = 0;
            while (currentPixel != null){
                double max = previousValues[col]; // find the above value
                int ref = col;

                // Check the left diagonal pixel
                if(col > 0 && previousValues[col-1] > max){
                    max = previousValues[col - 1];
                    ref = col - 1;
                }

                // Check the right diagonal pixel
                if(col < width - 1 && previousValues[col+1] > max){
                    max = previousValues[col+1];
                    ref = col + 1;
                }

                // Update the current pixel's value with the max sum path so far
                currentValues[col] = max + valueGetter.apply(currentPixel);

                // Store the best seam path leading to this pixel
                currentSeams.add(concat(currentPixel, previousSeams.get(ref)));

                // Move to the next pixel in the row
                col++;
                currentPixel = currentPixel.right;
            }

            // Prepare for the next row by updating references
            previousValues = currentValues;
            currentValues = new double[width];
            previousSeams = currentSeams;
            currentSeams = new ArrayList<>();
        }

        // find the seam with the max value
        double maxVal = previousValues[0];
        int maxValIndex = 0;


        for(int i = 0 ; i < width ; i++){
            if(previousValues[i] > maxVal){
                maxVal = previousValues[i];
                maxValIndex = i;
            }
        }

        // Return the seam with the highest total value
        return previousSeams.get(maxValIndex);
    }

    /**
     *
     * @param currentPixel
     * @param previousSeams
     * @return
     */
    public List<Pixel> concat(Pixel currentPixel, List<Pixel> previousSeams){
        // create a new Seams
        List<Pixel> newSeams = new ArrayList<>();
        // add current pixel to the front
        newSeams.add(currentPixel);
        // add all values of the previous seam
        newSeams.addAll(previousSeams);
        return newSeams;
    }
    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        //Or, since we haven't lectured on lambda syntax in Java, this can be
//        return getSeamMaximizing(new Function<Pixel, Double>() {
//            @Override
//            public Double apply(Pixel pixel) {
//                return pixel.getGreen();
//            }
//        });


    }

    public List<Pixel> getLowestEnergySeam() {
        calculateEnergy();
        /*
        Maximizing negation of energy is the same as minimizing the energy.
         */
        return getSeamMaximizing(pixel -> -pixel.energy);

        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return -pixel.energy;
            }
        });
        */
    }
}
