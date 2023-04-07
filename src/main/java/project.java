import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

//asks user for filepath
//runs files using fileProcessor with filepath constructor
//checks collisions using collisionCheck
//calculates percentage score
//outputs relevant data - saves to txt file

public class project {

    public static void main(String[] args) {

        // getting directory name
        System.out.println("Please enter a directory: ");
        Scanner s = new Scanner(System.in);
        String directoryName = s.nextLine();
        s.close();
        // System.out.println("Collecting zip files from directory: " + directoryName);
        // String directoryName = "C:\\Users\\phee3\\OneDrive\\Keele\\Keele 22-23\\30014 Dissertation Project\\test";
        System.out.println("\nCollecting zip files from directory: " + directoryName);

        // setting up file reader
        File directory = new File(directoryName);
        fileLoader fl = new fileLoader(directory);

        for (collusionFile cf : fl.collusionFiles) {
            collisionChecker cc = new collisionChecker(cf);
        }
        System.out.println("\nEnd Of Whitespace Check\n////////////////////////////////////////////////////////////");

        for (collusionFile cf : fl.collusionFiles) {
            if (fl.collusionFiles.indexOf(cf) + 1 < fl.collusionFiles.size()) {
                for (int i = fl.collusionFiles.indexOf(cf) + 1; i < fl.collusionFiles.size(); i++) {
                    System.out.println("\nComparison for " + cf.filename + " and " + fl.collusionFiles.get(i).filename);
                    collisionChecker cc = new collisionChecker(cf, fl.collusionFiles.get(i));
                }
            }
        }

        //calls collision checker for each file to get flags
        //then calls it with multiple files for comparison

        //prints out
        //iterates through files, shows file number, src file name, class names
        //shows each collision with the line where they start and the line where they end
        //shows the code of the beginning and ending line
    }
}
