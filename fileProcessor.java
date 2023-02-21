import java.io.*;
import java.util.*;

public class fileProcessor {
    // search for files in directory
    // unzip files
    // input file names into structure
    // iterate reading files through the structure of names
    // put files in different data structures, strip escape characters from
    // comments, potentially put comments in different data structure
    // tokenize method
    // put tokens in data structure

    // constructor that takes file directory
    public fileProcessor(File directory) {
        File filenames[] = directory.listFiles();
        // read other filenames from directory
    }

    public static void read(String[] args) {

        // things to fix
        // when searching a directory, need to go into a folder, then the src folder, then read all those classes into one file
        // need to find a way of concatenating files maybe? or maybe an arraylist of files for every source folder even if there's only one file, then when comparing for plagiarism can iterate through all arraylists.

        // iterate through
        ArrayList<File> files = new ArrayList<File>();
        ArrayList<String> filesAsStrings = new ArrayList<String>();
        // for (int i = 0; i < filenames; i++) {
        // try {
        // extract zip using zipinputstream
        // String filename = filenames[i].getPath(); 
        // File currentFile = new File(filename);
        // strip comments
        // read file into string
        // files.add(currentFile);
        // String currentFileString = new String;
        // Scanner reader = new Scanner(currentFile);
        // while (reader.hasNextLine()) {
        // String currentLine = reader.nextLine();
        // currentFileString += data;
        // }
        // reader.close();
        // } catch (FileNotFoundException e) {
        // System.out.println("An error occurred.");
        // e.printStackTrace();
        // }
        // }

    }

}
