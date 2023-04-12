import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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

        System.out.println("Please enter any code to not be included in the comparison, or press enter to continue: ");
        String givenCode = s.nextLine();
        s.close();
        // System.out.println("Collecting zip files from directory: " + directoryName);
        System.out.println("\n\nCollecting zip files from directory: " + directoryName);

        // setting up file reader
        File directory = new File(directoryName);
        fileLoader fl = new fileLoader(directory, givenCode);

        for (collusionFile cf : fl.collusionFiles) {
            collisionChecker cc = new collisionChecker(cf);
        }
//        System.out.println("\nEnd Of Whitespace Check\n////////////////////////////////////////////////////////////");

        ArrayList<scoreCalculator> results = new ArrayList<>();

        for (collusionFile cf : fl.collusionFiles) {
            if (fl.collusionFiles.indexOf(cf) + 1 < fl.collusionFiles.size()) {
                for (int i = fl.collusionFiles.indexOf(cf) + 1; i < fl.collusionFiles.size(); i++) {
//                    System.out.println("\nComparison for " + cf.filename + " and " + fl.collusionFiles.get(i).filename);
                    scoreCalculator sc = new scoreCalculator(cf, fl.collusionFiles.get(i));
                    results.add(sc);
                }
            }
        }
        Collections.sort(results, new Comparator<>() {
            @Override
            public int compare(scoreCalculator s1, scoreCalculator s2) {
                return Double.compare(s2.getScore(), s1.getScore());
            }
        });

        int rankTotal = 0;
        if (results.size() < 10) {
           rankTotal = results.size();
        }
        else {
            rankTotal = 10;
        }
        System.out.println("\n/////////////////////////////////////////////");
        System.out.println("The 10 most similar files are displayed below:\n");

        for (int i = 0; i < rankTotal; i++) {
            int rank = i+1;
            System.out.println("\n" + rank + ": " + results.get(i).getNames() + " - " + String.format("%.1f", results.get(i).getScore()) + "%" );
            System.out.println("blankLineResultsScore: " + String.format("%.1f", 80*results.get(i).blankLineResultsScore) + "%");
            System.out.println("startSpacesResultsScore: " + String.format("%.1f", 80*results.get(i).startSpacesResultsScore) + "%");
            System.out.println("endSpacesResultsScore: " + String.format("%.1f", 80*results.get(i).endSpacesResultsScore) + "%");
            System.out.println("startBracketsResultsScore: " + String.format("%.1f", 80*results.get(i).startBracketsResultsScore) + "%");
            System.out.println("endBracketResultsScore: " + String.format("%.1f", 80*results.get(i).endBracketResultsScore) + "%");
            System.out.println("ownBracketResultsScore: " + String.format("%.1f", 80*results.get(i).ownBracketResultsScore) + "%");
            System.out.println("fasSimilarity: " + String.format("%.1f", 80*results.get(i).fasSimilarity) + "%");
            System.out.println("commentSimilarity: " + String.format("%.1f", 80*results.get(i).commentSimilarity) + "%");
            System.out.println("tokenSimilarity: " + String.format("%.1f", 80*results.get(i).tokenSimilarity) + "%");
            System.out.println("commentTokenSimilarity: " + String.format("%.1f", 80*results.get(i).commentTokenSimilarity) + "%");
            System.out.println("outputSimilarity: " + String.format("%.1f", 80*results.get(i).outputSimilarity) + "%");
            System.out.println("substringSimilarity: " + String.format("%.1f", 80*results.get(i).substringSimilarity) + "%");
        }

        //calls collision checker for each file to get flags
        //then calls it with multiple files for comparison

        //prints out
        //iterates through files, shows file number, src file name, class names
        //shows each collision with the line where they start and the line where they end
        //shows the code of the beginning and ending line
    }
}
