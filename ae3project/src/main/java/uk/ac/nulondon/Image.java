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

        // find the horizontal energy
      double HorizEnergy = (above.left.brightness() + (2 * (current.left.brightness())) + below.left.brightness()) -
              (above.right.brightness() + (2 * (current.right.brightness())) + below.right.brightness());
        // find the vertical energy
      double VertEnergy = (above.left.brightness() + (2 * (above.brightness())) + above.right.brightness()) -
              (below.left.brightness() + (2 * (below.brightness())) + below.right.brightness());
        // find and return the energy
      return Math.sqrt(Math.pow(2, HorizEnergy) + Math.pow(2, VertEnergy));
    }

    /**
     * calculates the energy property of each pixel
     */
    public void calculateEnergy() {
        //TODO: calculate energy for all the pixels in the image
        int numRights = 0;
        for (int row = 0; row < rows.size(); row++){
            Pixel current = rows.get(row);
            while (current != null){
                System.out.println(row);
                if (row == 0 || row >= rows.size() - 1){
                    System.out.println("dfhjdas");
                    current.energy = current.brightness();
                }else{
                    Pixel above = rows.get(row - 1);
                    System.out.println(above);
                    Pixel below = rows.get(row + 1);
                    for (int i = numRights; i > 0; i--){
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
        List<Pixel> originalSeam =  new ArrayList<>(); //deep copy here
        for (int i = 0; i < getWidth(); i++){
            originalSeam.add(seam.get(i));
        }
        for (int i = 0; i < seam.size(); i++){ // highlight the seam
            seam.set(i, new Pixel(color));
         }
        return originalSeam; // return the original seam
    }

    /**
     *
     * @param seam
     */
    public void removeSeam(List<Pixel> seam) { //this might work this might not idk there is no way to test it right now
        //TODO: remove the provided seam
      //  for (Pixel pixel : seam) {
      //      row.right = row.legt;
     //   }
        for (int i = 0; i < seam.size(); i++) {
            Pixel current = rows.get(i);
            Pixel seamPixel = seam.get(i);
            while (current != null) {
                if (current == seamPixel) {
                    if(current.left != null){
                        current.left.right = current.right;
                    }
                    if(current.right != null){
                        current.right.left = current.left;
                    }
                    break;
                }
                current = current.right;
            }
        }
        width--;
    }

    public void addSeam(List<Pixel> seam) {
        //TODO: Add the provided seam
        for (int i = 0; i < seam.size(); i++) {
            Pixel rowHead = rows.get(i);
            Pixel seamPixel = seam.get(i);
            // if seamPixel should be insert at the beginning
            if(seamPixel.left == null){
                seamPixel.right = rowHead;
                rowHead.left = seamPixel;
                // update the row head to be the new seamPixel
                rows.set(i, seamPixel);
            } else{
                Pixel current = rowHead;
                while (current != null && current != seamPixel.right) {
                    current = current.right
                }
                // insert seamPixel between left neighbor and current
                seamPixel.left.right = seamPixel;
                if(current != null){
                    seamPixel.right = current;
                    current.left = seamPixel;
                } else{
                    seamPixel.right = null;
                }
            }
        }
        width++;
    }

    private List<Pixel> getSeamMaximizing(Function<Pixel, Double> valueGetter) {
        //TODO: find the seam which maximizes total value extracted from the given pixel
        //finds the seam with the highest energy and then returns it

        class Cell{
            Double value;
            Pixel pastPixel;

            public Cell(Double val, Pixel past){
                this.value = val;
                this.pastPixel = past;
            }
        }

        HashMap<Pixel, Cell> table = new HashMap<>();


        // add first row of values to table
        Pixel current = rows.get(0);
        while(current != null){
            Double val = valueGetter.apply(current);
            table.put(current, new Cell(val, null));
            current = current.right;
        }


        for (int row = 1; row < rows.size(); row++){ //we start at row 1 bc the row 0 does not have any above pixels
            current = rows.get(row);
            int numRights = 0;
            while (current != null){
                Pixel above = rows.get(row - 1); //gets the pixel above current
                for (int i = numRights; i > 0; i--){
                    above = above.right;
                }

                if (current.left == null){
                    //case for only having 2 above pixels (above and right)
                    double val = valueGetter.apply(current);
                    double valA = valueGetter.apply(above) + val;
                    double valR = valueGetter.apply(above.right) + val;
                    if (valA > valR){
                        table.put(current, new Cell(valA, above));
                    }else{
                        table.put(current, new Cell(valR, above.right));
                    }
                }else if (current.right == null){
                    //case for only having 2 above pixels (above and left)
                    double val = valueGetter.apply(current);
                    double valL = valueGetter.apply(above.left) + val;
                    double valA = valueGetter.apply(above) + val;
                    if (valL > valA){
                        table.put(current, new Cell(valL, above.left));
                    }else{
                        table.put(current, new Cell(valA, above));
                    }
                }else{
                    //we have all above pixels (left, above, right)
                    double val = valueGetter.apply(current);
                    double valL = valueGetter.apply(above.left) + val;
                    double valA = valueGetter.apply(above) + val;
                    double valR = valueGetter.apply(above.right) + val;
                    if (valL > valA){
                        if (valL > valR){
                            table.put(current, new Cell(valL, above.left));
                        }else{
                            table.put(current, new Cell(valR, above.right));
                        }
                    }else{
                        if (valA > valR){
                            table.put(current, new Cell(valA, above));
                        }else{
                            table.put(current, new Cell(valR, above.right));
                        }
                    }

                }
                current = current.right;
                numRights += 1;
            }
        }

        // get the last rows
        Pixel startingPixel = null;
        Double bestVal = (double) Integer.MIN_VALUE;
        Pixel currentPixel2 = rows.get(getHeight() -1);
        while (currentPixel2 != null){
            Double hashVal = table.get(currentPixel2).value;
            if (hashVal > bestVal){
                startingPixel = currentPixel2;
                bestVal = hashVal;
            }
            currentPixel2 = currentPixel2.right;
        }

        List<Pixel> seam = new ArrayList<>(); // list to store the path
        Pixel currentPixel = startingPixel; // last pixel of the image to start
        while(currentPixel != null && table.containsKey(currentPixel)){
            seam.add(currentPixel); // add the current pixel to list
            currentPixel = table.get(currentPixel).pastPixel; // move to the new pixel
        }

        Collections.reverse(seam);

        return seam;
    }

    public List<Pixel> getGreenestSeam() {
        return getSeamMaximizing(Pixel::getGreen);
        /*Or, since we haven't lectured on lambda syntax in Java, this can be
        return getSeamMaximizing(new Function<Pixel, Double>() {
            @Override
            public Double apply(Pixel pixel) {
                return pixel.getGreen();
            }
        });*/

    }

    public List<Pixel> getLowestEnergySeam() {
        System.out.println("BHjkbjksbh");
        calculateEnergy();
        System.out.println("njfksala");
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
