import java.util.ArrayList;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import opennlp.tools.tokenize.SimpleTokenizer;

import static java.lang.Math.abs;

public class collisionChecker {
    ArrayList<String> fileByLine;
    LevenshteinDistance l;
    LongestCommonSubsequence lcs;
    String fn1;
    String fas1;
    ArrayList<String> tf1;
    ArrayList<String> tfnk1;
    String cas1;
    ArrayList<String> tc1;
    String fn2;
    String fas2;
    ArrayList<String> tf2;
    ArrayList<String> tfnk2;
    String cas2;
    ArrayList<String> tc2;
    public boolean formattingFlag;
    public boolean whitespaceFlag;
    public int over2BlankLinesFlag;
    public int over2BlankLinesPC;
    public ArrayList<Integer> over2BlankLinesIndices;
    public int endOfLineSpacesFlag;
    public int endSpacesPC;
    public ArrayList<Integer> endSpacesIndices;
    public int startOfLineSpacesFlag;
    public int startSpacesPC;
    public ArrayList<Integer> startSpacesIndices;
    public int printSimilarity;
    public double fasSimilarity;
    public double commentSimilarity;
    boolean usernameMatch;

    public collisionChecker(collusionFile f) {
        //constructor for identifying flags that dont need comparing
        fn1 = f.filename;
        fas1 = f.fileAsString;
        tf1 = f.tokenizedFile;
        tfnk1 = f.tokenFileNoKeywords;
        cas1 = f.commentsAsString;
        tc1 = f.tokenizedComments;

        whitespaceCheck(f);
        formattingCheck(f);
    }

    public collisionChecker(collusionFile f1, collusionFile f2) {
        //overloaded constructor for comparison
        fn1 = f1.filename;
        fas1 = f1.fileAsString;
        tf1 = f1.tokenizedFile;
        tfnk1 = f1.tokenFileNoKeywords;
        cas1 = f1.commentsAsString;
        tc1 = f1.tokenizedComments;

        fn2 = f2.filename;
        fas2 = f2.fileAsString;
        tf2 = f2.tokenizedFile;
        tfnk2 = f2.tokenFileNoKeywords;
        cas2 = f2.commentsAsString;
        tc2 = f2.tokenizedComments;

        l = new LevenshteinDistance();
        lcs = new LongestCommonSubsequence();

        usernameMatch = false;
        printSimilarity = 0;
        fasSimilarity = 0;

        outputCheck(f1, f2);
        basicCheck(f1, f2);
        substringCheck(f1, f2);

        //flag comparison for each of the whitespace flag arrays and each of the formatting flag arrays
    }

    public int getLineNumber(collusionFile f, int charNo) {
        for (int i = 0; i < f.newLines.size(); i++) {
            if (charNo <= f.newLines.get(i)) {
                return i;
            }
        }
        return f.newLines.size();
    }

    public double percentageScore(String string1, String string2, int similarity) {
        if (similarity == 0) {
            return 100;
        }
        String longString;
        if (string1.length() >= string2.length()) {
            longString = string1;
        } else {
            longString = string2;
        }
        return (longString.length() - similarity) / ((double) longString.length()) * 100;
    }


    public void formattingCheck(collusionFile f) {

        ArrayList<Integer> newLines = new ArrayList<Integer>();
        ArrayList<Integer> formattingIndices = new ArrayList<Integer>();
        ArrayList<Integer> endOfLineBracket = new ArrayList<Integer>();
        ArrayList<Integer> startOfLineBracket = new ArrayList<Integer>();
        ArrayList<Integer> ownLineBracket = new ArrayList<Integer>();

        int endOfLineFlag = 0;
        int startOfLineFlag = 0;
        int ownLineFlag = 0;

        for (int i = 0; i < fas1.length(); i++) {
            if (fas1.charAt(i) == '\n') {
                newLines.add(i);
            }
        }

        for (int i = 0; i < fas1.length(); i++) {
            if (fas1.charAt(i) == '{') {
                formattingIndices.add(i);
            }
        }

        for (int i = 0; i < formattingIndices.size(); i++) {
            int diff = 100;
            int closestNL = 0;
            int nextClosestNL = 0;
            String line = "";
            String beforeLine = "";
            String afterLine = "";
            for (int j = 0; j < newLines.size(); j++) {
                if (abs((formattingIndices.get(i) - newLines.get(j))) < diff) {
                    diff = abs((formattingIndices.get(i) - newLines.get(j)));
                    closestNL = j;
                }
            }
            if (formattingIndices.get(i) < newLines.get(closestNL)) {
                nextClosestNL = closestNL - 1;
                line = fas1.substring(newLines.get(nextClosestNL), newLines.get(closestNL));
                beforeLine = fas1.substring(newLines.get(nextClosestNL), formattingIndices.get(i) - 1);
                afterLine = fas1.substring(formattingIndices.get(i) + 1, newLines.get(closestNL));
            } else {
                nextClosestNL = closestNL + 1;
                line = fas1.substring(newLines.get(closestNL), newLines.get(nextClosestNL));
                beforeLine = fas1.substring(newLines.get(closestNL), formattingIndices.get(i) - 1);
                afterLine = fas1.substring(formattingIndices.get(i) + 1, newLines.get(nextClosestNL));
            }

            if (beforeLine.isBlank() && afterLine.isBlank()) {
                ownLineBracket.add(formattingIndices.get(i));
            } else if (beforeLine.isBlank()) {
                endOfLineBracket.add(formattingIndices.get(i));
            } else if (afterLine.isBlank()) {
                startOfLineBracket.add(formattingIndices.get(i));
            }
        }

        //ownline biggest
        if (ownLineBracket.size() >= endOfLineBracket.size() && ownLineBracket.size() >= startOfLineBracket.size()) {
            if (endOfLineBracket.size() > (0.15 * ownLineBracket.size()) && endOfLineBracket.size() < (0.5 * ownLineBracket.size())) {
                endOfLineFlag++;
            }
            if (startOfLineBracket.size() > (0.15 * ownLineBracket.size()) && startOfLineBracket.size() < (0.5 * ownLineBracket.size())) {
                startOfLineFlag++;
            }
        }
        //endofline biggest
        else if (endOfLineBracket.size() >= ownLineBracket.size() && endOfLineBracket.size() >= startOfLineBracket.size()) {
            if (startOfLineBracket.size() > (0.15 * endOfLineBracket.size()) && startOfLineBracket.size() < (0.5 * endOfLineBracket.size())) {
                startOfLineFlag++;
            }
            if (ownLineBracket.size() > (0.15 * endOfLineBracket.size()) && ownLineBracket.size() < (0.5 * endOfLineBracket.size())) {
                ownLineFlag++;
            }
        }
        //startofline biggest
        else {
            if (endOfLineBracket.size() > (0.15 * startOfLineBracket.size()) && endOfLineBracket.size() < (0.5 * startOfLineBracket.size())) {
                endOfLineFlag++;
            }
            if (ownLineBracket.size() > (0.15 * startOfLineBracket.size()) && ownLineBracket.size() < (0.5 * startOfLineBracket.size())) {
                ownLineFlag++;
            }
        }

        if (ownLineFlag > 0) {
            System.out.println("Formatting flag (new line { used infrequently) at ");
            for (int i = 0; i < ownLineBracket.size(); i++) {
                System.out.println(ownLineBracket.get(i) + ",");
            }
        }
        if (startOfLineFlag > 0) {
            System.out.println("Formatting flag (start of line { used infrequently) at ");
            for (int i = 0; i < startOfLineBracket.size(); i++) {
                System.out.println(startOfLineBracket.get(i) + ",");
            }
        }
        if (endOfLineFlag > 0) {
            System.out.println("Formatting flag (end of line { used infrequently) at ");
            for (int i = 0; i < endOfLineBracket.size(); i++) {
                System.out.println(endOfLineBracket.get(i) + ",");
            }
        }

        f.endOfLineFlag = endOfLineFlag;
        f.startOfLineFlag = startOfLineFlag;
        f.ownLineFlag = ownLineFlag;
        f.formattingIndices = formattingIndices;
        f.endOfLineBracket = endOfLineBracket;
        f.startOfLineBracket = startOfLineBracket;
        f.ownLineBracket = ownLineBracket;
    }

    public void flagComparison(ArrayList<Integer> flagIndexF1, ArrayList<Integer> flagIndexF2) {
        ArrayList<ArrayList<Integer>> stringIndices = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> startIndex = new ArrayList<Integer>();
        ArrayList<Integer> endIndex = new ArrayList<Integer>();
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        for (int i = 0; i < flagIndexF1.size(); i++) {
            for (int j = 0; j < flagIndexF2.size(); j++) {
                //backwards check
                String beforeString1 = fas1.substring(0, flagIndexF1.get(i) - 1);
                String[] tokenBeforeString1 = tokenizer.tokenize(beforeString1);
                String beforeString2 = fas2.substring(0, flagIndexF2.get(j) - 1);
                String[] tokenBeforeString2 = tokenizer.tokenize(beforeString2);
                //forwards check
                String afterString1 = fas1.substring(flagIndexF1.get(i) + 1, fas1.length() - 1);
                String[] tokenAfterString1 = tokenizer.tokenize(afterString1);
                String afterString2 = fas2.substring(flagIndexF2.get(j) + 1, fas2.length() - 1);
                String[] tokenAfterString2 = tokenizer.tokenize(afterString2);

                //iterate backwards through tokenbefore arrays, until find one that doesn't match
                int beforeCounter = 0;
                int counterF1 = tokenAfterString1.length-1;
                int counterF2 = tokenAfterString2.length-1;
                boolean keywordFlag = false;

                backTokenLoop:
                while (counterF1 >= 0 && counterF2 >= 0) {
                    if (tokenAfterString1[counterF1].equals(tokenAfterString2[counterF2])) {
                        beforeCounter++;
                        keywordFlag = false;
                    }
                    else {
                        for (String keyword: fileLoader.keywords) {
                            if (keyword.matches(tokenAfterString1[counterF1])) {
                                if (keywordFlag) {
                                    //add index to start index
                                    //fix keyword logic - keywords are not the same as variable names
                                    //loop needs to break if an unmatching keyword is found, or if multiple unmatching variable names are found
                                    break backTokenLoop;
                                }
                                keywordFlag = true;
                            }
                        }
                    }
                    counterF1--;
                    counterF2--;
                }
            }
        }

        // (potential to see if variable names changed) - if the differing characters form a substring, compare its similarity to protected keywords
        //if it doesn't match, or if the string contains less than a certain percentage of protected keywords in its length, it could be a variable or text block
        //to translate - find string of non-matching characters in line with high similarity, compare to list of protected keywords, if no matches, and if next characters are ( or {, flag as potentially changed variable name
    }

    public void basicCheck(collusionFile f1, collusionFile f2) {
        if (f1.filename.matches(f2.filename)) {
            usernameMatch = true;
            System.out.println("Filenames match - author " + f1.filename + " has the same student number as author " + f2.filename);
        } else {
            System.out.println("Filenames do not match");
        }

        fasSimilarity = percentageScore(fas1, fas2, l.apply(fas1, fas2));
        System.out.println("Distance between files as strings: " + l.apply(fas1, fas2));
        System.out.println("File as string similarity: " + String.format("%.1f", fasSimilarity) + "%");

        commentSimilarity = percentageScore(cas1, cas2, l.apply(cas1, cas2));
        System.out.println("Distance between comments as strings: " + l.apply(cas1, cas2));
        System.out.println("Comments as string similarity: " + String.format("%.1f", commentSimilarity) + "%");

        for (String token1 : tfnk1) {
            for (String token2 : tfnk2) {
                double similarity = percentageScore(token1, token2, l.apply(token1, token2));
                if (similarity > 80) {
                    System.out.println("Similar token identified at " + String.format("%.1f", similarity) + "% similarity, tokens are: " + token1 + " (line " + getLineNumber(f1, fas1.indexOf(token1)) + " of " + fn1 + ") and " + token2 + " (line " + getLineNumber(f2, fas2.indexOf(token2)) + " of file " + fn2 + ")");
                }
            }
        }

        for (String token1 : tc1) {
            for (String token2 : tc2) {
                double similarity = percentageScore(token1, token2, l.apply(token1, token2));
                if (similarity > 80) {
                    System.out.println("Similar comment token identified at " + String.format("%.1f", similarity) + "% similarity, tokens are: " + token1 + " (line " + getLineNumber(f1, fas1.indexOf(token1)) + " of " + fn1 + ") and " + token2 + " (line " + getLineNumber(f2, fas2.indexOf(token2)) + " of file " + fn2 + ")");
                }
            }
        }

        for (int i = 0; i < f1.fileByLine.size(); i++) {
            for (int j = 0; j < f2.fileByLine.size(); j++) {
                double stringSimilarity = percentageScore(f1.fileByLine.get(i), f2.fileByLine.get(j), l.apply(f1.fileByLine.get(i), f2.fileByLine.get(j)));
                if (stringSimilarity > 50) {
                    System.out.println(String.format("%.1f", stringSimilarity) + "% line similarity found in file " + f1.filename + " at line " + i + ", and file " + f2.filename + " at line " + j + " (can print line here)");
                }
            }
        }
    }

    public void substringCheck(collusionFile f1, collusionFile f2) {
        String subsequence = lcs.longestCommonSubsequence(f1.fileAsString, f2.fileAsString).toString();
        int length = subsequence.length();
        double PCofF1 = 100 * length / (double) f1.fileAsString.length();
        double PCofF2 = 100 * length / (double) f2.fileAsString.length();
        int indexF1 = f1.fileAsString.indexOf(subsequence);
        int indexF2 = f2.fileAsString.indexOf(subsequence);

        if (PCofF1 > 20 || PCofF2 > 20) {
            System.out.println("Long subsequence found in file " + f1.filename + " at line " + getLineNumber(f1, indexF1) + " (" + String.format("%.1f", PCofF1) + "% of file length) and in file " + f2.filename + " at line " + getLineNumber(f2, indexF2) + " (" + String.format("%.1f", PCofF2) + "% of file length)");
        }
    }

    public void outputCheck(collusionFile f1, collusionFile f2) {
        ArrayList<Integer> printIndices1 = new ArrayList<Integer>();
        ArrayList<Integer> printIndices2 = new ArrayList<Integer>();
        String printsf1 = "";
        String printsf2 = "";

        int i1 = f1.fileAsString.indexOf("System.out.print");
        while (i1 >= 0) {
            printIndices1.add(i1);
            i1 = f1.fileAsString.indexOf("System.out.print", i1 + 1);
        }
        for (int value : printIndices1) {
            //indexof ( and indexof )
            //create substring between these indices
            int start;
            int end;
            if (f1.fileAsString.charAt(f1.fileAsString.indexOf('(', value) + 1) == '"') {
                start = f1.fileAsString.indexOf('(', value) + 2;
            } else {
                start = f1.fileAsString.indexOf('(', value) + 1;
            }
            if (f1.fileAsString.charAt(f1.fileAsString.indexOf('(', value) - 1) == '"') {
                end = f1.fileAsString.indexOf(')', value) - 2;
            } else {
                end = f1.fileAsString.indexOf(')', value) - 1;
            }
            printsf1 += f1.fileAsString.substring(start, end);
        }

        int i2 = f2.fileAsString.indexOf("System.out.print");
        while (i2 >= 0) {
            printIndices2.add(i2);
            i2 = f2.fileAsString.indexOf("System.out.print", i2 + 1);
        }
        for (int value : printIndices2) {
            int start;
            int end;
            if (f2.fileAsString.charAt(f2.fileAsString.indexOf('(', value) + 1) == '"') {
                start = f2.fileAsString.indexOf('(', value) + 2;
            } else {
                start = f2.fileAsString.indexOf('(', value) + 1;
            }
            if (f2.fileAsString.charAt(f2.fileAsString.indexOf('(', value) - 1) == '"') {
                end = f2.fileAsString.indexOf(')', value) - 2;
            } else {
                end = f2.fileAsString.indexOf(')', value) - 1;
            }
            printsf2 += f2.fileAsString.substring(start, end);
        }
        System.out.println("Print String from " + f1.filename + ": " + printsf1);
        System.out.println("Print String from " + f2.filename + ": " + printsf2);
        printSimilarity = l.apply(printsf1.toLowerCase(), printsf2.toLowerCase());
        double similarityPC = percentageScore(printsf1, printsf2, printSimilarity);
        System.out.println("Distance between " + f1.filename + " and " + f2.filename + ": " + printSimilarity);
        System.out.println("Percentage Similarity: " + String.format("%.1f", similarityPC) + "%");
    }

    public void whitespaceCheck(collusionFile f) {

        ArrayList<Integer> newLines = new ArrayList<Integer>();
        endOfLineSpacesFlag = 0;
        startOfLineSpacesFlag = 0;
        over2BlankLinesFlag = 0;
        over2BlankLinesIndices = new ArrayList<Integer>();
        endSpacesIndices = new ArrayList<Integer>();
        startSpacesIndices = new ArrayList<Integer>();
        fileByLine = new ArrayList<String>();
        over2BlankLinesPC = 0;
        startSpacesPC = 0;
        endSpacesPC = 0;

        int blankLines = 0;

        for (int i = 0; i < fas1.length(); i++) {
            if (fas1.charAt(i) == '\n') {
                newLines.add(i);
                if (i > 0) {
                    fileByLine.add(f.fileAsString.substring(newLines.get(newLines.size() - 2) + 1, i));
                }
            }
        }

        int numberofLines = newLines.size();
        System.out.println("\nNumber of Lines: " + numberofLines + "\nWhitespace Flags: ");

        for (int i = 0; i < fas1.length(); i++) {
            int j = 1;
            while ((i + j) < fas1.length() && fas1.charAt(i) == '\n' && fas1.charAt(i + j) == '\n') {
                j++;
                blankLines++;
                System.out.println("Blank Lines: " + blankLines + " (char before: " + fas1.charAt(i - 1) + ", " + (i - 1) + ")");
            }
            if (j > 2) {
                over2BlankLinesFlag++;
                over2BlankLinesIndices.add(i);
            }
            i += (j - 1);
        }
        System.out.println("Blank Lines: " + blankLines + ", Over 2 Blank Lines Flag: " + over2BlankLinesFlag);

        for (int i = 0; i < over2BlankLinesIndices.size(); i++) {
//            System.out.println(over2BlankLinesIndices.get(i));
        }

        //checking how many spaces before and after new line
        for (int i = 0; i < newLines.size(); i++) {
//            System.out.println(fn1 + ": Line " + (i + 1) + ", total lines " + newLines.size());
            int startCounter = 1;
            int endCounter = 1;
            while ((newLines.get(i) - endCounter >= 0) && fas1.charAt(newLines.get(i) - endCounter) == ' ') {
                endCounter++;
            }

            while ((newLines.get(i) + startCounter < fas1.length()) && fas1.charAt(newLines.get(i) + startCounter) == ' ') {
                startCounter++;
            }
            if ((startCounter - 1) % 4 != 0) {
                startOfLineSpacesFlag++;
                startSpacesIndices.add(newLines.get(i));
//                System.out.println("Start of Line Flags: " + startOfLineSpacesFlag + ", number of spaces: " + ((startCounter - 1) % 4));
            }
            // for end counter, decrement the line number, as this references the PREVIOUS lines end spaces
            if (endCounter > 1) {
                endOfLineSpacesFlag++;
                endSpacesIndices.add(newLines.get(i));
//                System.out.println("End of Line Flags: " + endOfLineSpacesFlag + ", number of spaces: " + (endCounter - 1));
            }
        }

        over2BlankLinesPC = over2BlankLinesFlag / numberofLines;
        startSpacesPC = startOfLineSpacesFlag / numberofLines;
        endSpacesPC = endOfLineSpacesFlag / numberofLines;

        f.over2BlankLinesFlag = over2BlankLinesFlag;
        f.over2BlankLinesPC = over2BlankLinesPC;
        f.over2BlankLinesIndices = over2BlankLinesIndices;
        f.endOfLineSpacesFlag = endOfLineSpacesFlag;
        f.endSpacesPC = endSpacesPC;
        f.endSpacesIndices = endSpacesIndices;
        f.startOfLineSpacesFlag = startOfLineSpacesFlag;
        f.startSpacesPC = startSpacesPC;
        f.startSpacesIndices = startSpacesIndices;
        f.newLines = newLines;
        f.blankLines = blankLines;
        f.fileByLine = fileByLine;
    }
}
