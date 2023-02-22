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
        System.out.println("Collecting files from directory: " + directoryName);

        // setting up file reader
        File directory = new File(directoryName);
        fileProcessor fp = new fileProcessor(directory);
        fp.read();
    }
}
