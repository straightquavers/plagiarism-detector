import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Array;
import java.util.*;

//asks user for filepath
//runs files using fileProcessor with filepath constructor
//checks collisions using collisionCheck
//calculates percentage score
//outputs relevant data - saves to txt file

public class project {

    public static void main(String[] args) {

        // getting directory name
        System.out.println("////////////////////////////////////////////////////////////////////////////////////////////////////////////////" +
                "\nThis program identifies potential plagiarism in Java source code, from user-submitted zipped folders containing Java classes." +
                "\nTo use the program, please enter a directory name when prompted. Only zipped folders will be identified by the program." +
                "\nFolders with multiple Java classes and other folders can be used, as long as the following structure is included: internal folder named 'src' -> Java class(es)." +
                "\nThen, provide any Java code to be excluded from the detection process (such as code provided as part of a programming assignment)." +
                "\nThe program will output the files with highest plagiarism scores above 50% (maximum 10 files displayed), and create a text file in the provided directory with more detailed information." +
                "////////////////////////////////////////////////////////////////////////////////////////////////////////////////" +
                "\n\nPlease enter a directory: ");
        Scanner s = new Scanner(System.in);
        String directoryName = s.nextLine();

        System.out.println("\nPlease enter any code to not be included in the comparison, or press enter to continue: ");
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
        for (scoreCalculator sc : results) {
            if (sc.getScore() > 50) {
                rankTotal++;
            }
        }
        System.out.println("\n///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////");
        System.out.println("\nA text file containing more detailed results has been created in the provided directory." +
                "\nFiles with similarity score over 50% are displayed below:\n");

        for (int i = 0; i < rankTotal; i++) {
            int rank = i + 1;

            System.out.println(rank + ": " + results.get(i).getNames().get(2) + " - " + String.format("%.1f", results.get(i).getScore()) + "%");
//            System.out.println("blankLineResultsScore: " + String.format("%.1f", 80 * results.get(i).blankLineResultsScore) + "%");
//            System.out.println("startSpacesResultsScore: " + String.format("%.1f", 80 * results.get(i).startSpacesResultsScore) + "%");
//            System.out.println("endSpacesResultsScore: " + String.format("%.1f", 80 * results.get(i).endSpacesResultsScore) + "%");
//            System.out.println("startBracketsResultsScore: " + String.format("%.1f", 80 * results.get(i).startBracketsResultsScore) + "%");
//            System.out.println("endBracketResultsScore: " + String.format("%.1f", 80 * results.get(i).endBracketResultsScore) + "%");
//            System.out.println("ownBracketResultsScore: " + String.format("%.1f", 80 * results.get(i).ownBracketResultsScore) + "%");
//            System.out.println("fasSimilarity: " + String.format("%.1f", 80 * results.get(i).fasSimilarity) + "%");
//            System.out.println("commentSimilarity: " + String.format("%.1f", 80 * results.get(i).commentSimilarity) + "%");
//            System.out.println("tokenSimilarity: " + String.format("%.1f", 80 * results.get(i).tokenSimilarity) + "%");
//            System.out.println("commentTokenSimilarity: " + String.format("%.1f", 80 * results.get(i).commentTokenSimilarity) + "%");
//            System.out.println("outputSimilarity: " + String.format("%.1f", 80 * results.get(i).outputSimilarity) + "%");
//            System.out.println("substringSimilarity: " + String.format("%.1f", 80 * results.get(i).substringSimilarity) + "%");
        }

        // print to text file:
        // each individual score, ranked in order (if not 0)
        // each flag with name, char index start and line number start, char index end and line number end

        try {
            String resultsFileName = directoryName + "\\plagiarism-results.txt";
            File resultsFile = new File(resultsFileName);
            if (resultsFile.exists()) {
                resultsFile.delete();
            }
            resultsFile.createNewFile();
            FileWriter writer = new FileWriter(resultsFileName);
            writer.write("/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////" +
                    "\nThis file gives more detailed information about the generated plagiarism scores." +
                    "\nThe individual simliarity metrics are shown, along with the line numbers of any identified flags." +
                    "\nThe individual metrics are defined as follows:" +
                    "\n\nFile Overall Similarity: The Levenshtein edit distance between both files, represented as a percentage of the average file length." +
                    "\n\nComment Overall Similarity: The Levenshtein edit distance between the comments in both files, represented as a percentage of the total length of all comments." +
                    "\n\nSimilarity between Tokenised Files: The percentage of file tokens (words in the file excluding punctuation and protected keywords) that are more than 80% similar." +
                    "\n\nSimilarity between Tokenised Comments: The percentage of comment tokens (words in any comments excluding punctuation and protected keywords) that are more than 80% similar." +
                    "\n\nSimilarity Between Terminal Output: The Levenshtein edit distance between the contents of any print statements, represented as a percentage of the total size of the outputs." +
                    "\n\nLongest Similar Substring as % of File Length: The longest substring with no edit distance between the two files, represented as a percentage of the average file length." +
                    "\n\nInconsistent Whitespace Usage Flags (multiple blank lines): The number of instances of multiple consecutive blank lines correlating with matching substrings between the files, represented as a percentage." +
                    "\n\nInconsistent Whitespace Usage Flags (spaces at start of line): The number of instances of incorrect indentation at the start of lines correlating with matching substrings between the files, represented as a percentage." +
                    "\n\nInconsistent Whitespace Usage Flags (spaces at end of line): The number of instances of multiple spaces used at the end of lines correlating with matching substrings between the files, represented as a percentage." +
                    "\n\nInconsistent Formatting Flags (brackets used at start of line infrequently): The number of instances of an inconsistent formatting style used (using opening brackets at the start of a line atypically), correlating with matching substrings between the files, represented as a percentage." +
                    "\n\nInconsistent Formatting Flags (brackets used at end of line infrequently): The number of instances of an inconsistent formatting style used (using opening brackets at the end of a line atypically), correlating with matching substrings between the files, represented as a percentage." +
                    "\n\nInconsistent Formatting Flags (brackets used on their own line infrequently): The number of instances of an inconsistent formatting style used (using opening brackets on a blank line atypically), correlating with matching substrings between the files, represented as a percentage." +
                    "\n\n/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////" +
                    "\n\nResults:");
            int rank = 1;
            for (scoreCalculator sc : results) {
                writer.write("\n\n\nRank " + rank + ": " + sc.getNames().get(2) + " - " + String.format("%.1f", sc.getScore()) + "%: ");
                rank++;
                ArrayList<ArrayList<?>> rankedList = new ArrayList<>();

                //add names and values
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Whitespace Usage Flags (multiple blank lines)", sc.blankLineResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Whitespace Usage Flags (spaces at start of line)", sc.startSpacesResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Whitespace Usage Flags (spaces at end of line)", sc.endSpacesResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Formatting Flags (brackets used at start of line infrequently)", sc.startBracketsResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Formatting Flags (brackets used at end of line infrequently)", sc.endBracketResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Inconsistent Formatting Flags (brackets used on their own line infrequently)", sc.ownBracketResultsScore)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("File Overall Similarity as Strings", sc.fasSimilarity)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Comment Overall Similarity as Strings", sc.commentSimilarity)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Similarity between Tokenised Files", sc.tokenSimilarity)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Similarity between Tokenised Comments", sc.commentTokenSimilarity)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Similarity Between Terminal Output", sc.outputSimilarity)));
                rankedList.add(new ArrayList<>(
                        Arrays.asList("Longest Similar Substring as % of File Length", sc.substringSimilarity)));

//                Collections.sort(rankedList, Collections.reverseOrder());
                Collections.sort(rankedList, new Comparator<ArrayList<?>>() {
                    @Override
                    public int compare(ArrayList<?> score1, ArrayList<?> score2) {
                        Double value1 = ((Number) score1.get(1)).doubleValue();
                        Double value2 = ((Number) score2.get(1)).doubleValue();
                        return value2.compareTo(value1);
                    }
                });

                for (int i = 0; i < rankedList.size(); i++) {
                    Double value = ((Number) rankedList.get(i).get(1)).doubleValue();
                    if (value > 0) {
                        writer.write("\n" + (i + 1) + ": " + rankedList.get(i).get(0) + " - " + String.format("%.1f", 80 * value) + "%");
                    }
                }
//                blankLineResults
//                startSpacesResults
//                endSpacesResults
//                startBracketsResults
//                endBracketResults
//                ownBracketResults
                //strings indices file 1, file 2 ->
                // list of all flags ->
                // flag index f1, flag index f2, start index f1, end index f1

                writer.write("\n\nThe following whitespace and formatting flags have been identified at the following locations:");
//                for (int i = 0; i < sc.getIndices().size(); i++) {

                int blankLinesCounter = 0;
                ArrayList<Integer> blankLinesIndices1 = new ArrayList<>();
                ArrayList<Integer> blankLinesIndices2 = new ArrayList<>();
                if (sc.getIndices().get(0).get(0).size() > 0) {
                    while (blankLinesCounter < sc.getIndices().get(0).get(0).size() && sc.getIndices().get(0).get(0).get(blankLinesCounter).size() > 0 && sc.getIndices().get(0).get(1).get(blankLinesCounter).size() > 0) {
                        if (!blankLinesIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(0).get(0).get(blankLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(0).get(0).get(blankLinesCounter).get(0));
                            blankLinesIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(0).get(0).get(blankLinesCounter).get(0)));
                        }
                        if (!blankLinesIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(0).get(1).get(blankLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(0).get(1).get(blankLinesCounter).get(0));
                            blankLinesIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(0).get(1).get(blankLinesCounter).get(0)));
                        }
                        blankLinesCounter++;
                    }
                    writer.write("\nInconsistent Whitespace Usage (multiple blank lines) - Located between lines " + blankLinesIndices1.get(0) + "-" + blankLinesIndices1.get(blankLinesIndices1.size() - 1));
//                        for (int j : blankLinesIndices1) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f1.filename + ", and between lines " + blankLinesIndices2.get(0) + "-" + blankLinesIndices2.get(blankLinesIndices2.size() - 1));
//                        for (int j : blankLinesIndices2) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f2.filename);
                }


                int startLinesCounter = 0;
                ArrayList<Integer> startLinesIndices1 = new ArrayList<>();
                ArrayList<Integer> startLinesIndices2 = new ArrayList<>();
                if (sc.getIndices().get(1).get(0).size() > 0) {
                    while (startLinesCounter < sc.getIndices().get(1).get(0).size() && sc.getIndices().get(1).get(0).get(startLinesCounter).size() > 0 && sc.getIndices().get(1).get(1).get(startLinesCounter).size() > 0) {
                        if (!startLinesIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(1).get(0).get(startLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(1).get(0).get(startLinesCounter).get(0));
                            startLinesIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(1).get(0).get(startLinesCounter).get(0)));
                        }
                        if (!startLinesIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(1).get(1).get(startLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(1).get(1).get(startLinesCounter).get(0));
                            startLinesIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(1).get(1).get(startLinesCounter).get(0)));
                        }
                        startLinesCounter++;
                    }
                    writer.write("\nInconsistent Whitespace Usage (incorrect indentation at start of lines) - Located between lines " + startLinesIndices1.get(0) + "-" + startLinesIndices1.get(startLinesIndices1.size() - 1));
//                        for (int j : startLinesIndices1) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f1.filename + ", and between lines " + startLinesIndices2.get(0) + "-" + startLinesIndices2.get(startLinesIndices2.size() - 1));
//                        for (int j : startLinesIndices2) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f2.filename);
                }


                int endLinesCounter = 0;
                ArrayList<Integer> endLinesIndices1 = new ArrayList<>();
                ArrayList<Integer> endLinesIndices2 = new ArrayList<>();
                if (sc.getIndices().get(2).get(0).size() > 0) {
                    while (endLinesCounter < sc.getIndices().get(2).get(0).size() && sc.getIndices().get(2).get(0).get(endLinesCounter).size() > 0 && sc.getIndices().get(2).get(1).get(endLinesCounter).size() > 0) {
                        if (!endLinesIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(2).get(0).get(endLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(2).get(0).get(endLinesCounter).get(0));
                            endLinesIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(2).get(0).get(endLinesCounter).get(0)));
                        }
                        if (!endLinesIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(2).get(1).get(endLinesCounter).get(0)))) {
//                                System.out.println(sc.getIndices().get(2).get(1).get(endLinesCounter).get(0));
                            endLinesIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(2).get(1).get(endLinesCounter).get(0)));
                        }
                        endLinesCounter++;
                    }

                    writer.write("\nInconsistent Whitespace Usage (incorrect indentation at end of lines) - Located between lines " + endLinesIndices1.get(0) + "-" + endLinesIndices1.get(endLinesIndices1.size() - 1));
//                        for (int j : endLinesIndices1) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f1.filename + ", and between lines " + endLinesIndices2.get(0) + "-" + endLinesIndices2.get(endLinesIndices2.size() - 1));
//                        for (int j : endLinesIndices2) {
//                            writer.write(j + ", ");
//                        }
                    writer.write(" in " + sc.f2.filename);
                }


                int startFormCounter = 0;
                ArrayList<Integer> startFormIndices1 = new ArrayList<>();
                ArrayList<Integer> startFormIndices2 = new ArrayList<>();
                if (sc.startBracketsResultsScore > 0) {
                    if (sc.getIndices().get(3).get(0).size() > 0) {
                        while (startFormCounter < sc.getIndices().get(3).get(0).size() && sc.getIndices().get(3).get(0).get(startFormCounter).size() > 0 && sc.getIndices().get(3).get(1).get(startFormCounter).size() > 0) {
                            if (!startFormIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(3).get(0).get(startFormCounter).get(0)))) {
//                                    System.out.println(sc.getIndices().get(3).get(0).get(startFormCounter).get(0));
                                startFormIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(3).get(0).get(startFormCounter).get(0)));
                            }
                            if (!startFormIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(3).get(1).get(startFormCounter).get(0)))) {
                                startFormIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(3).get(1).get(startFormCounter).get(0)));
//                                    System.out.println(sc.getIndices().get(3).get(1).get(startFormCounter).get(0));
                            }
                            startFormCounter++;
                        }

                        writer.write("\nInconsistent Formatting (brackets used at start of line infrequently) - Located between lines " + startFormIndices1.get(0) + "-" + startFormIndices1.get(startFormIndices1.size()-1));
//                        for (int j : startFormIndices1) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f1.filename + ", and between lines " + startFormIndices2.get(0) + "-" + startFormIndices2.get(startFormIndices2.size()-1));
//                        for (int j : startFormIndices2) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f2.filename);
                    }
                }


                int endFormCounter = 0;
                ArrayList<Integer> endFormIndices1 = new ArrayList<>();
                ArrayList<Integer> endFormIndices2 = new ArrayList<>();
                if (sc.endBracketResultsScore > 0) {
                    if (sc.getIndices().get(4).get(0).size() > 0) {
                        while (endFormCounter < sc.getIndices().get(4).get(0).size() && sc.getIndices().get(4).get(0).get(endFormCounter).size() > 0 && sc.getIndices().get(4).get(1).get(endFormCounter).size() > 0) {
                            if (!endFormIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(4).get(0).get(endFormCounter).get(0)))) {
//                                    System.out.println(sc.getIndices().get(4).get(0).get(endFormCounter).get(0));
                                endFormIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(4).get(0).get(endFormCounter).get(0)));
                            }
                            if (!endFormIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(4).get(1).get(endFormCounter).get(0)))) {
//                                    System.out.println(sc.getIndices().get(4).get(1).get(endFormCounter).get(0));
                                endFormIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(4).get(1).get(endFormCounter).get(0)));
                            }
                            endFormCounter++;
                        }

                        writer.write("\nInconsistent Formatting (brackets used at end of line infrequently) - Located between lines " + endFormIndices1.get(0) + "-" + endFormIndices1.get(endFormIndices1.size()-1));
//                        for (int j : endFormIndices1) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f1.filename + ", and between lines " + endFormIndices2.get(0) + "-" + endFormIndices2.get(endFormIndices2.size()-1));
//                        for (int j : endFormIndices2) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f2.filename);
                    }
                }


                int ownFormCounter = 0;
                ArrayList<Integer> ownFormIndices1 = new ArrayList<>();
                ArrayList<Integer> ownFormIndices2 = new ArrayList<>();
                if (sc.ownBracketResultsScore < 0) {
                    if (sc.getIndices().get(5).get(0).size() > 0) {
                        while (ownFormCounter < sc.getIndices().get(5).get(0).size() && sc.getIndices().get(5).get(0).get(ownFormCounter).size() > 0 && sc.getIndices().get(5).get(1).get(ownFormCounter).size() > 0) {
                            if (!ownFormIndices1.contains(sc.getLineNumber(sc.f1, sc.getIndices().get(5).get(0).get(ownFormCounter).get(0)))) {
//                                    System.out.println(sc.getIndices().get(5).get(0).get(ownFormCounter).get(0));
                                ownFormIndices1.add(sc.getLineNumber(sc.f1, sc.getIndices().get(5).get(0).get(ownFormCounter).get(0)));
                            }
                            if (!ownFormIndices2.contains(sc.getLineNumber(sc.f2, sc.getIndices().get(5).get(1).get(ownFormCounter).get(0)))) {
//                                    System.out.println(sc.getIndices().get(5).get(1).get(ownFormCounter).get(0));
                                ownFormIndices2.add(sc.getLineNumber(sc.f2, sc.getIndices().get(5).get(1).get(ownFormCounter).get(0)));
                            }
                            ownFormCounter++;
                        }

                        writer.write("\nInconsistent Formatting (brackets used on their own line infrequently) - Located between lines " + ownFormIndices1.get(0) + "-" + ownFormIndices1.get(ownFormIndices1.size()-1));
//                        for (int j : ownFormIndices1) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f1.filename + ", and between lines " + ownFormIndices2.get(0) + "-" + ownFormIndices2.get(ownFormIndices2.size()-1));
//                        for (int j : ownFormIndices2) {
//                            writer.write(j + ", ");
//                        }
                        writer.write(" in " + sc.f2.filename);
                    }
                }
//                }
            }

            writer.close();

        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
