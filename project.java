import java.io.File;
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
        System.out.println("Collecting zip files from directory: " + directoryName);

        // setting up file reader
        File directory = new File(directoryName);
        fileLoader fl = new fileLoader(directory);
    }
}
