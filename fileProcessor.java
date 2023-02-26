// import java.io.*;
// import java.util.*;
// import java.util.zip.*;

// public class fileProcessor {
//     // search for files in directory
//     // unzip files
//     // input file names into structure
//     // iterate reading files through the structure of names
//     // put files in different data structures, strip escape characters from
//     // comments, potentially put comments in different data structure
//     // tokenize method
//     // put tokens in data structure
//     private static File directory;
//     private String[] filenames;
//     private File[] files;


//     public void read() {

//         // things to fix
//         // when searching a directory, need to go into a folder, then the src folder,
//         // then read all those classes into one file
//         // need to find a way of concatenating files maybe? or maybe an arraylist of
//         // files for every source folder even if there's only one file, then when
//         // comparing for plagiarism can iterate through all arraylists.

//         // iterate through
//         ArrayList<ArrayList<File>> files = new ArrayList<ArrayList<File>>();
//         ArrayList<String> filesAsStrings = new ArrayList<String>();
//         // for (int i = 0; i < filenames.length; i++) {
//         //     try {
//         //         ArrayList<File> classesPerFile = new ArrayList<File>();
//         //         // go into directory
//         //         String filename = filenames[i];
//         //         String filepath = directory + "\\" + filename;
//         //         File currentFile = new File(filepath);
//         //         ArrayList<File> classes = new ArrayList<File>();
//         //         // extract zip using zipinputstream
//         //         System.out.println("----"+filename);

//         //         ZipInputStream readZip = new ZipInputStream(new FileInputStream(currentFile));
//         //         for (int j = 0; j < 10; j++) {
//         //             ZipEntry entry = readZip.getNextEntry();
//         //             System.out.println(entry.getName());

//         //             //if it ends in .java to find source files
//         //             if (entry.getName() == filename + "/src/") {
//         //                 classes.add(new File)
//         //                 String currentFileString = new String();
//         //                 for (int k = 0; k < classes.length; k++) {
//         //                     // add to arraylist of classes per file
//         //                     classesPerFile.add(new File(currentFile, classes[k].getName()));
//         //                     Scanner reader = new Scanner(classes[k]);
//         //                     while (reader.hasNextLine()) {
//         //                         String currentLine = reader.nextLine();
//         //                         currentFileString += currentLine;
//         //                     }
//         //                     reader.close();
//         //                     files.add(classesPerFile);
//         //                     filesAsStrings.add(currentFileString);
//         //                     System.out.println(currentFileString);
//         //                 }
//         //             }
//         //         }
//         //         readZip.close();
//         //     } catch (IOException e) {
//         //         System.out.println("An error occurred. Please restart and enter a valid directory");
//         //         e.printStackTrace();
//         //     }
   
//         // }

//     }


//     public void processFile(String fp) throws Exception {
//         // // Test that the file ends in .zip
//         // if (!getExt(fp).equals("zip")) {
//         //     throw new Exception("File is not a zip file");
//         // }
//         File zipFile = new File(fp);

//         // Extract to the tmp directory
//         // unzip(zipFile.getAbsolutePath(), tempDirectory.getAbsolutePath());

//         // Find all .java files in the tmp directory
        

//         // For each, Tokenise (Seperate method probably)
//         // TODO

//     }


// }
