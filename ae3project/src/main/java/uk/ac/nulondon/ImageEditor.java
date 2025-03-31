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
        //TODO: implement via Command pattern
        List<Pixel> seam = image.getGreenestSeam(); //gets the seam
        Command highlight = new HighlightGreenest(seam); //creates the command
        remote.executeCommand(highlight); //puts the command via the remote to actually run the command
    }

    public void removeHighlighted() throws IOException {
        //TODO: implement via Command pattern
    }

    public void undo() throws IOException {
        //TODO: implement via Command pattern
    }

    public void highlightLowestEnergySeam() throws IOException {
        //TODO: implement via Command pattern
    }

    //TODO: implement Command class or interface and its subtypes


    class HighlightGreenest implements Command{
        List<Pixel> seam;
        public HighlightGreenest(List<Pixel> seam){
            this.seam = seam;
        }
        @Override
        public void execute(){
            //i think this is where we do the code to actually highlight in the image
            List<Pixel> originalSeam = image.higlightSeam(seam, Color.GREEN);
            seam = originalSeam;
        }

        @Override
        public void undo(){

        }
    }

}
