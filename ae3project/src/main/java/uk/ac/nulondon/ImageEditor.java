package uk.ac.nulondon;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

/*APPLICATION SERVICE LAYER*/
public class ImageEditor {

    private Image image;

    private List<Pixel> highlightedSeam = null;

    private Remote remote = new Remote();

    interface Command{
        void execute();

        void undo();
    }

    class Remote{
        private Stack<Command> undoStack = new Stack<>();
        private Command command;

        public void executeCommand(Command command){
            command.execute();
            undoStack.push(command);
        }

        public void undo(){
            if (!undoStack.isEmpty()){
                Command command = undoStack.pop();
                command.undo();
            }else{
                System.out.println("Nothing to undo here...");
            }
        }
    }

    public void load(String filePath) throws IOException {
        File originalFile = new File(filePath);
        BufferedImage img = ImageIO.read(originalFile);
        image = new Image(img);
    }

    public void save(String filePath) throws IOException {
        BufferedImage img = image.toBufferedImage();
        ImageIO.write(img, "png", new File(filePath));
    }

    public void highlightGreenest() throws IOException {
        System.out.println("6");
        try{
            System.out.println("7");
            List<Pixel> seam = image.getGreenestSeam(); //gets the seam
            System.out.println("8");
            Command highlight = new HighlightGreenest(seam); //creates the command
            System.out.println("9");
            remote.executeCommand(highlight); //puts the command via the remote to actually run the command
            System.out.println("10");
        }  catch (Exception e) {
            throw new IOException(e);
        }

    }

    public void removeHighlighted() throws IOException {
        try {
            System.out.println("HighlightSeam: " + highlightedSeam);
            Command removeHighlight = new RemoveHighlighted(highlightedSeam);
            remote.executeCommand(removeHighlight);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void undo() throws IOException {
        try {
            remote.undo();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public void highlightLowestEnergySeam() throws IOException {
        try {
            List<Pixel> seam = image.getLowestEnergySeam();
            Command lowestEnergyHighlight = new HighlightLowestEnergy(seam);
            remote.executeCommand(lowestEnergyHighlight);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    class HighlightGreenest implements Command{
        private List<Pixel> seam;
        public HighlightGreenest(List<Pixel> seam){
            this.seam = seam;
        }
        @Override
        public void execute(){
            highlightedSeam = image.higlightSeam(seam, Color.GREEN);
        }

        @Override
        public void undo(){
            image.addSeam(highlightedSeam); //this is either highlightedSeam or seam
        }
    }


    class HighlightLowestEnergy implements Command{
        private List<Pixel> seam;
        public HighlightLowestEnergy(List<Pixel> seam){
            this.seam = seam;
        }
        @Override
        public void execute(){
            highlightedSeam = image.higlightSeam(seam, Color.RED);
        }

        @Override
        public void undo(){
            image.addSeam(highlightedSeam); //this is either highlightedSeam or seam
        }
    }

    class RemoveHighlighted implements Command{
        private List<Pixel> seam;
        public RemoveHighlighted(List<Pixel> seam){
            this.seam = seam;
        }
        @Override
        public void execute(){
            image.removeSeam(seam);
        }

        @Override
        public void undo(){
            image.addSeam(seam);
        }
    }

    public List<Pixel> getRows(){
        return image.rows;
    }

}
