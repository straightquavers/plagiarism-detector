import java.util.ArrayList;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import opennlp.tools.tokenize.SimpleTokenizer;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class collisionChecker {
    double substringScore;
    double fileByLineScore;
    double similarityPC;
    double tokenScore;
    double commentTokenScore;
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

    ArrayList<ArrayList<ArrayList<Integer>>> blankLineResults;
    double blankLineResultsScore;
    ArrayList<ArrayList<ArrayList<Integer>>> startSpacesResults;
    double startSpacesResultsScore;
    ArrayList<ArrayList<ArrayList<Integer>>> endSpacesResults;
    double endSpacesResultsScore;
    ArrayList<ArrayList<ArrayList<Integer>>> startBracketsResults;
    double startBracketsResultsScore;
    ArrayList<ArrayList<ArrayList<Integer>>> endBracketResults;
    double endBracketResultsScore;
    ArrayList<ArrayList<ArrayList<Integer>>> ownBracketResults;
    double ownBracketResultsScore;
    int tokenCounter;
    int commentTokenCounter;

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

        blankLineResults = flagComparison(f1, f2, f1.over2BlankLinesIndices, f2.over2BlankLinesIndices);
        blankLineResultsScore = flagResultCalculator(blankLineResults, f1, f2);
        startSpacesResults = flagComparison(f1, f2, f1.startSpacesIndices, f2.startSpacesIndices);
        startSpacesResultsScore = flagResultCalculator(startSpacesResults, f1, f2);
        endSpacesResults = flagComparison(f1, f2, f1.endSpacesIndices, f2.endSpacesIndices);
        endSpacesResultsScore = flagResultCalculator(endSpacesResults, f1, f2);

        startBracketsResults = new ArrayList<>();
        endBracketResults = new ArrayList<>();
        ownBracketResults = new ArrayList<>();

        if (f1.formattingIndicator.contains("s") || f2.formattingIndicator.contains("s")) {
            startBracketsResults = flagComparison(f1, f2, f1.startOfLineBracket, f2.startOfLineBracket);
            startBracketsResultsScore = flagResultCalculator(startBracketsResults, f1, f2);
        }
        if (f1.formattingIndicator.contains("e") || f2.formattingIndicator.contains("e")) {
            endBracketResults = flagComparison(f1, f2, f1.endOfLineBracket, f2.endOfLineBracket);
            endBracketResultsScore = flagResultCalculator(endBracketResults, f1, f2);
        }
        if (f1.formattingIndicator.contains("o") || f2.formattingIndicator.contains("o")) {
            ownBracketResults = flagComparison(f1, f2, f1.ownLineBracket, f2.ownLineBracket);
            ownBracketResultsScore = flagResultCalculator(ownBracketResults, f1, f2);
        }
    }

    public double flagResultCalculator(ArrayList<ArrayList<ArrayList<Integer>>> results, collusionFile f1, collusionFile f2) {
        double score1 = 0;
        double score2 = 0;
        double score = 0;
        for (int i = 0; i < results.size(); i++) {
            int stringLength = 0;
            int numberOfStrings = results.get(i).size();
            for (int j = 0; j < numberOfStrings; j++) {
                // remove matching from arrays - if arraylist.get(1) == j
                stringLength += results.get(i).get(j).get(2) + results.get(i).get(j).get(3);
            }
            collusionFile file;
            if (i == 0) {
                score1 = stringLength/(double)f1.fileAsString.length();
            }
            else {
                score2 = stringLength/(double)f2.fileAsString.length();
            }
            score = max(score1,score2);
        }

//        System.out.println(score);
        return score;

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
            return 1;
        }
        String longString;
        if (string1.length() >= string2.length()) {
            longString = string1;
        } else {
            longString = string2;
        }
        return (longString.length() - similarity) / ((double) longString.length());
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

        for (int i = 0; i < f.fileAsString.length(); i++) {
            if (f.fileAsString.charAt(i) == '\n') {
                newLines.add(i);
            }
        }

        for (int i = 0; i < f.fileAsString.length(); i++) {
            if (f.fileAsString.charAt(i) == '{') {
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
                line = f.fileAsString.substring(newLines.get(nextClosestNL), newLines.get(closestNL));
                beforeLine = f.fileAsString.substring(newLines.get(nextClosestNL), formattingIndices.get(i) - 1);
                afterLine = f.fileAsString.substring(formattingIndices.get(i) + 1, newLines.get(closestNL));
            } else {
                nextClosestNL = closestNL + 1;
                line = f.fileAsString.substring(newLines.get(closestNL), newLines.get(nextClosestNL));
                beforeLine = f.fileAsString.substring(newLines.get(closestNL), formattingIndices.get(i) - 1);
                afterLine = f.fileAsString.substring(formattingIndices.get(i) + 1, newLines.get(nextClosestNL));
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
//            if (endOfLineBracket.size() > (0.1 * ownLineBracket.size()) && endOfLineBracket.size() < (0.7 * ownLineBracket.size())) {
                endOfLineFlag++;
//            }
//            if (startOfLineBracket.size() > (0.1 * ownLineBracket.size()) && startOfLineBracket.size() < (0.7 * ownLineBracket.size())) {
                startOfLineFlag++;
//            }
        }
        //endofline biggest
        else if (endOfLineBracket.size() >= ownLineBracket.size() && endOfLineBracket.size() >= startOfLineBracket.size()) {
//            if (startOfLineBracket.size() > (0.10 * endOfLineBracket.size()) && startOfLineBracket.size() < (0.7 * endOfLineBracket.size())) {
                startOfLineFlag++;
//            }
//            if (ownLineBracket.size() > (0.10 * endOfLineBracket.size()) && ownLineBracket.size() < (0.7 * endOfLineBracket.size())) {
                ownLineFlag++;
//            }
        }
        //startofline biggest
        else {
//            if (endOfLineBracket.size() > (0.1 * startOfLineBracket.size()) && endOfLineBracket.size() < (0.7 * startOfLineBracket.size())) {
                endOfLineFlag++;
//            }
//            if (ownLineBracket.size() > (0.1 * startOfLineBracket.size()) && ownLineBracket.size() < (0.7 * startOfLineBracket.size())) {
                ownLineFlag++;
//            }
        }

        if (ownLineFlag > 0) {
//            System.out.println("Formatting flag (new line { used infrequently) at ");
            f.formattingIndicator += 'o';
            for (int i = 0; i < ownLineBracket.size(); i++) {
//                System.out.println(ownLineBracket.get(i) + ",");
            }
        }
        if (startOfLineFlag > 0) {
//            System.out.println("Formatting flag (start of line { used infrequently) at ");
            f.formattingIndicator += 's';
            for (int i = 0; i < startOfLineBracket.size(); i++) {
//                System.out.println(startOfLineBracket.get(i) + ",");
            }
        }
        if (endOfLineFlag > 0) {
//            System.out.println("Formatting flag (end of line { used infrequently) at ");
            f.formattingIndicator += 'e';
            for (int i = 0; i < endOfLineBracket.size(); i++) {
//                System.out.println(endOfLineBracket.get(i) + ",");
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

    public ArrayList<ArrayList<ArrayList<Integer>>> flagComparison(collusionFile f1, collusionFile f2, ArrayList<Integer> flagIndexF1, ArrayList<Integer> flagIndexF2) {
        ArrayList<ArrayList<ArrayList<Integer>>> returnIndices = new ArrayList<>();
        ArrayList<ArrayList<Integer>> stringIndices1 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> stringIndices2 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> stringIndices = new ArrayList<>();

        for (int i = 0; i < flagIndexF1.size(); i++) {
            for (ArrayList<Integer> al : stringIndices1) {
                if (flagIndexF1.get(i) <= al.get(3) && flagIndexF1.get(i) >= al.get(2) && (i+1) < flagIndexF1.size()) {
                    i++;
                }
            }
            for (int j = 0; j < flagIndexF2.size(); j++) {
                for (ArrayList<Integer> al : stringIndices2) {
                    if (flagIndexF2.get(j) <= al.get(3) && flagIndexF2.get(j) >= al.get(2) && (j+1) < flagIndexF2.size()) {
                        j++;
                    }
                }
                ArrayList<Integer> tempIndices1 = new ArrayList<>();
                ArrayList<Integer> tempIndices2 = new ArrayList<>();
                tempIndices1.add(flagIndexF1.get(i));
                tempIndices1.add(flagIndexF2.get(j));
                tempIndices2.add(flagIndexF2.get(j));
                tempIndices2.add(flagIndexF1.get(i));

                String beforeString1 = fas1.substring(0, flagIndexF1.get(i));
                String beforeString2 = fas2.substring(0, flagIndexF2.get(j));
                String afterString1 = fas1.substring(flagIndexF1.get(i), fas1.length());
                String afterString2 = fas2.substring(flagIndexF2.get(j), fas2.length());

                int beforeCounter = 1;
                int afterCounter = 1;

                beforeLoop:
                while (beforeCounter < flagIndexF1.get(i) && beforeCounter < flagIndexF2.get(j)) {
                    if (beforeString1.charAt(beforeString1.length() - beforeCounter) == beforeString2.charAt(beforeString2.length() - beforeCounter)) {
                        beforeCounter++;
                    } else {
                        break beforeLoop;
                    }
                }

                tempIndices1.add(flagIndexF1.get(i) - beforeCounter);
                tempIndices2.add(flagIndexF2.get(j) - beforeCounter);

                afterLoop:
                while (afterCounter + flagIndexF1.get(i) < fas1.length() && flagIndexF2.get(j) + afterCounter < fas2.length()) {
                    if (afterString1.charAt(afterCounter) == afterString2.charAt(afterCounter)) {
                        afterCounter++;
                    } else {
                        break afterLoop;
                    }
                }

                tempIndices1.add(flagIndexF1.get(i) + afterCounter);
                tempIndices2.add(flagIndexF2.get(j) + afterCounter);
                int stringLength = beforeCounter + afterCounter;

//                if (stringLength > 0.05 * f1.fileAsString.length()) {
                    stringIndices1.add(tempIndices1);
//                }
//                if (stringLength > 0.05 * f2.fileAsString.length()) {
                    stringIndices2.add(tempIndices2);
//                }


//                System.out.println("Flag string found of length " + stringLength + " in " + f1.filename + " from char " + stringIndices1.get(i).get(0) + " to char " + stringIndices1.get(i).get(1) + ", and in " + f2.filename + " from char " + stringIndices2.get(j).get(0) + " to char " + stringIndices2.get(j).get(1));

            }
        }
        for (ArrayList<Integer> al : stringIndices1) {
            int stringLength = al.get(2) + al.get(3);
            if (al.get(1) < stringIndices2.size()) {
//                System.out.println("Flag string found of length " + stringLength + " in " + f1.filename + " from char " + al.get(2) + " to char " + al.get(3) + ", and in " + f2.filename + " from char " + stringIndices2.get(al.get(1)).get(2) + " to char " + stringIndices2.get(al.get(1)).get(3));
            }
        }

        returnIndices.add(stringIndices1);
        returnIndices.add(stringIndices2);
        return returnIndices;


    }
//    public ArrayList<ArrayList<ArrayList<Integer>>> flagComparison2(ArrayList<Integer> flagIndexF1, ArrayList<Integer> flagIndexF2) {
//        ArrayList<ArrayList<ArrayList<Integer>>> returnIndices = new ArrayList<>();
//        ArrayList<ArrayList<Integer>> stringIndices1 = new ArrayList<ArrayList<Integer>>();
//        ArrayList<Integer> startIndex1 = new ArrayList<Integer>();
//        ArrayList<Integer> endIndex1 = new ArrayList<Integer>();
//
//        ArrayList<ArrayList<Integer>> stringIndices2 = new ArrayList<ArrayList<Integer>>();
//        ArrayList<Integer> startIndex2 = new ArrayList<Integer>();
//        ArrayList<Integer> endIndex2 = new ArrayList<Integer>();
//
//        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
//        for (int i = 0; i < flagIndexF1.size(); i++) {
//            for (int j = 0; j < flagIndexF2.size(); j++) {
//                ArrayList<Integer> tempIndices1 = new ArrayList<Integer>();
//                ArrayList<Integer> tempIndices2 = new ArrayList<Integer>();
//
//                //backwards check
//                String beforeString1 = fas1.substring(0, flagIndexF1.get(i) - 1);
//                String[] tokenBeforeString1 = tokenizer.tokenize(beforeString1);
//                String beforeString2 = fas2.substring(0, flagIndexF2.get(j) - 1);
//                String[] tokenBeforeString2 = tokenizer.tokenize(beforeString2);
//                //forwards check
//                String afterString1 = fas1.substring(flagIndexF1.get(i) + 1, fas1.length() - 1);
//                String[] tokenAfterString1 = tokenizer.tokenize(afterString1);
//                String afterString2 = fas2.substring(flagIndexF2.get(j) + 1, fas2.length() - 1);
//                String[] tokenAfterString2 = tokenizer.tokenize(afterString2);
//
//                //iterate backwards through tokenbefore arrays, until find one that doesn't match
//                int beforeCharIndex1 = 0;
//                int beforeCharIndex2 = 0;
//                int afterCharIndex1 = 0;
//                int afterCharIndex2 = 0;
//
//                int beforeCounter = 0;
//                int beforeCharCounter1 = 1;
//                int beforeCharCounter2 = 1;
//                int beforeCounterF1 = tokenBeforeString1.length - 1;
//                int beforeCounterF2 = tokenBeforeString2.length - 1;
//                boolean beforeKeywordFlag = false;
//
//                int afterCounter = 0;
//                int afterCharCounter1 = 1;
//                int afterCharCounter2 = 1;
//                int afterCounterF1 = tokenAfterString1.length - 1;
//                int afterCounterF2 = tokenAfterString2.length - 1;
//                boolean afterKeywordFlag = false;
//
//                backTokenLoop:
//                while (beforeCounterF1 >= 0 && beforeCounterF2 >= 0 && beforeCounterF1 < tokenBeforeString1.length && beforeCounterF2 < tokenBeforeString2.length) {
//                    if (tokenBeforeString1[beforeCounterF1].matches("\\p{Punct}{1,}")) {
//                        String[] beforeFix1 = tokenBeforeString1[beforeCounterF1].split("\\p{Punct}{1,}");
//                        tokenBeforeString1[beforeCounterF1] = "";
//                        for (String s : beforeFix1) {
//                            s = "\\\\" + s;
//                            tokenBeforeString1[beforeCounterF1] += s;
//                        }
//                    }
//                    if (tokenBeforeString2[beforeCounterF2].matches("\\p{Punct}{1,}")) {
//                        String[] beforeFix2 = tokenBeforeString2[beforeCounterF2].split("\\p{Punct}{1,}");
//                        tokenBeforeString2[beforeCounterF2] = "";
//                        for (String s : beforeFix2) {
//                            s = "\\\\" + s;
//                            tokenBeforeString2[beforeCounterF2] += s;
//                        }
//                    }
//
//                    if (tokenBeforeString1[beforeCounterF1].equals(tokenBeforeString2[beforeCounterF2]) && !tokenBeforeString1[beforeCounterF1].isBlank()) {
//                        beforeCounter++;
//                        beforeKeywordFlag = false;
////                        System.out.println("Tokens match: " + tokenBeforeString1[beforeCounterF1] + " and " + tokenBeforeString2[beforeCounterF2]);
//
//                    } else {
//                        for (String keyword : fileLoader.keywords) {
//                            if (!keyword.matches(tokenBeforeString1[beforeCounterF1])) {
//                                if (beforeKeywordFlag) {
//                                    beforeCharCounter1 += tokenBeforeString1[beforeCounterF1].length() + 1;
//                                    beforeCharCounter2 += tokenBeforeString2[beforeCounterF2].length() + 1;
//                                    beforeCharIndex1 = flagIndexF1.get(i) - beforeCharCounter1;
//                                    beforeCharIndex2 = flagIndexF2.get(j) - beforeCharCounter2;
//                                    break backTokenLoop;
//                                }
//                                beforeKeywordFlag = true;
//                            } else {
//                                beforeCharIndex1 = flagIndexF1.get(i) - beforeCharCounter1;
//                                beforeCharIndex2 = flagIndexF2.get(j) - beforeCharCounter2;
//                                break backTokenLoop;
//                            }
//                        }
//                    }
//                    beforeCharCounter1 += tokenBeforeString1[beforeCounterF1].length() + 1;
//                    beforeCharCounter2 += tokenBeforeString2[beforeCounterF2].length() + 1;
//                    beforeCounterF1--;
//                    beforeCounterF2--;
//                }
//
//                tempIndices1.add(flagIndexF1.get(i)-beforeCharIndex1);
//                tempIndices2.add(flagIndexF2.get(j)-beforeCharIndex2);
//
//                //remove all startindex lines, here startindex.add(startchar - beforecharcounter) for 1 and 2
//
//                afterTokenLoop:
//                while (afterCounterF1 >= 0 && afterCounterF2 >= 0 && afterCounterF1 < tokenAfterString1.length && afterCounterF2 < tokenAfterString2.length) {
//                    if (tokenAfterString1[afterCounterF1].matches("\\p{Punct}{1,}")) {
//                        String[] afterFix1 = tokenAfterString1[afterCounterF1].split("\\p{Punct}{1,}");
//                        tokenAfterString1[afterCounterF1] = "";
//                        for (String s : afterFix1) {
//                            s = "\\\\" + s;
//                            tokenAfterString1[afterCounterF1] += s;
//                        }
//                    }
//                    if (tokenAfterString2[afterCounterF2].matches("\\p{Punct}{1,}")) {
//                        String[] afterFix2 = tokenAfterString2[afterCounterF2].split("\\p{Punct}{1,}");
//                        tokenAfterString2[afterCounterF2] = "";
//                        for (String s : afterFix2) {
//                            s = "\\\\" + s;
//                            tokenAfterString2[afterCounterF2] += s;
//                        }
//                    }
//                    if (tokenAfterString1[afterCounterF1].equals(tokenAfterString2[afterCounterF2]) && !tokenAfterString1[afterCounterF1].isBlank()) {
//                        afterCounter++;
//                        afterKeywordFlag = false;
////                        System.out.println("Tokens match: " + tokenAfterString1[afterCounterF1] + " and " + tokenAfterString2[afterCounterF2]);
//                    } else {
//                        for (String keyword : fileLoader.keywords) {
//                            if (!keyword.matches(tokenAfterString1[afterCounterF1])) {
//                                if (afterKeywordFlag) {
//                                    afterCharCounter1 += tokenAfterString1[afterCounterF1].length() + 1;
//                                    afterCharCounter2 += tokenAfterString2[afterCounterF2].length() + 1;
//                                    afterCharIndex1 = flagIndexF1.get(i) - afterCharCounter1;
//                                    afterCharIndex2 = flagIndexF2.get(j) - afterCharCounter2;
//                                    break afterTokenLoop;
//                                }
//                                afterKeywordFlag = true;
//                            } else {
//                                afterCharIndex1 = flagIndexF1.get(i) - afterCharCounter1;
//                                afterCharIndex2 = flagIndexF2.get(j) - afterCharCounter2;
//                                break afterTokenLoop;
//                            }
//                        }
//                    }
//                    afterCharCounter1 += tokenAfterString1[afterCounterF1].length() + 1;
//                    afterCharCounter2 += tokenAfterString2[afterCounterF2].length() + 1;
//                    afterCounterF1--;
//                    afterCounterF2--;
//                }
//
//                tempIndices1.add(flagIndexF1.get(i)+afterCharIndex1);
//                tempIndices2.add(flagIndexF2.get(j)+afterCharIndex2);
//
//                stringIndices1.add(tempIndices1);
//                stringIndices2.add(tempIndices2);
//
//                int stringLength1 = stringIndices1.get(i).get(1) - stringIndices1.get(i).get(0);
//                        int stringLength2 = stringIndices2.get(j).get(1) - stringIndices2.get(j).get(0);
////                System.out.println("Flag string found of length " + stringLength1 + " for first file, and " + stringLength2 + " for second file.");
//
//                returnIndices.add(stringIndices1);
//                returnIndices.add(stringIndices2);
//
//            }
//        }
//        return returnIndices;
//    }

    public void basicCheck(collusionFile f1, collusionFile f2) {
        if (f1.filename.matches(f2.filename)) {
            usernameMatch = true;
//            System.out.println("Filenames match - author " + f1.filename + " has the same student number as author " + f2.filename);
        } else {
//            System.out.println("Filenames do not match");
        }

        fasSimilarity = percentageScore(fas1, fas2, l.apply(fas1, fas2));
//        System.out.println("Distance between files as strings: " + l.apply(fas1, fas2));
//        System.out.println("File as string similarity: " + String.format("%.1f", fasSimilarity) + "%");

        commentSimilarity = percentageScore(cas1, cas2, l.apply(cas1, cas2));
//        System.out.println("Distance between comments as strings: " + l.apply(cas1, cas2));
//        System.out.println("Comments as string similarity: " + String.format("%.1f", commentSimilarity) + "%");

        tokenCounter = 0;
        for (String token1 : tfnk1) {
            for (String token2 : tfnk2) {
                double similarity = percentageScore(token1, token2, l.apply(token1, token2));
                if (similarity > 80) {
                    tokenCounter++;
//                    System.out.println("Similar token identified at " + String.format("%.1f", similarity) + "% similarity, tokens are: " + token1 + " (line " + getLineNumber(f1, fas1.indexOf(token1)) + " of " + fn1 + ") and " + token2 + " (line " + getLineNumber(f2, fas2.indexOf(token2)) + " of file " + fn2 + ")");
                }
            }
        }
        tokenScore = ((tokenCounter/ (double)tfnk1.size()) + (tokenCounter/ (double)tfnk2.size()));

        if (tokenCounter > tfnk1.size() / 2) {
//            System.out.println("More than half the tokens have >80% similarity to the other file");
        }
        if (tokenCounter > tfnk2.size() / 2) {
//            System.out.println("More than half the tokens have >80% similarity to the other file");
        }

        commentTokenCounter = 0;
        for (String token1 : tc1) {
            for (String token2 : tc2) {
                double similarity = percentageScore(token1, token2, l.apply(token1, token2));
                if (similarity > 80) {
                    commentTokenCounter++;
//                    System.out.println("Similar comment token identified at " + String.format("%.1f", similarity) + "% similarity, tokens are: " + token1 + " (line " + getLineNumber(f1, fas1.indexOf(token1)) + " of " + fn1 + ") and " + token2 + " (line " + getLineNumber(f2, fas2.indexOf(token2)) + " of file " + fn2 + ")");
                }
            }
        }
        if (tc1.size() > 0 && tc2.size() > 0) {
            commentTokenScore = ((commentTokenCounter / (double) tc1.size()) + (commentTokenCounter / (double) tc2.size()));
//            System.out.println(commentTokenScore);
        }
        else {
            commentTokenScore = 0;
        }

        int fileByLineCounter = 0;
        for (int i = 0; i < f1.fileByLine.size(); i++) {
            for (int j = 0; j < f2.fileByLine.size(); j++) {
                double stringSimilarity = percentageScore(f1.fileByLine.get(i), f2.fileByLine.get(j), l.apply(f1.fileByLine.get(i), f2.fileByLine.get(j)));
                if (stringSimilarity > 70) {
                    fileByLineCounter++;
//                    System.out.println(String.format("%.1f", stringSimilarity) + "% line similarity found in file " + f1.filename + " at line " + i + ", and file " + f2.filename + " at line " + j + " (can print line here)");
                }
            }
        }

        fileByLineScore = ((fileByLineCounter/ (double)f1.newLines.size()) + (fileByLineCounter/ (double)f2.newLines.size()));

        if (fileByLineCounter > f1.newLines.size() / 2) {
//            System.out.println("More than half of the lines in " + f1.filename + " are copied.");
        }
        if (fileByLineCounter > f2.newLines.size() / 2) {
//            System.out.println("More than half of the lines in " + f2.filename + " are copied.");
        }
    }

    public void substringCheck(collusionFile f1, collusionFile f2) {
        int length1 = f1.fileAsString.length();
        int length2 = f2.fileAsString.length();
        int substringLength = 0;
        int startIndex1 = 0;
        int startIndex2 = 0;
        int endIndex1 = 0;
        int endIndex2 = 0;

        for (int i = 0; i < length1; i++) {
            for (int j = 0; j < length2; j++) {
                int length = 0;
                int counter1 = i;
                int counter2 = j;
                while (counter1 < length1 && counter2 < length2 && f1.fileAsString.charAt(counter1) == f2.fileAsString.charAt(counter2)) {
                    length++;
                    counter1++;
                    counter2++;
                }
                if (length > substringLength) {
                    substringLength = length;
                    endIndex1 = counter1;
                    endIndex2 = counter2;
                }
            }
        }

        startIndex1 = endIndex1 - substringLength;
        startIndex2 = endIndex2 - substringLength;

        double PCofF1 = substringLength / (double) f1.fileAsString.length();
        double PCofF2 = substringLength / (double) f2.fileAsString.length();

        substringScore = (PCofF1 + PCofF2)/2;

        if (PCofF1 > 20) {
//            System.out.println("Long subsequence found in file " + f1.filename + " at line " + getLineNumber(f1, startIndex1) + " (" + String.format("%.1f", PCofF1) + "% of file length) and in file " + f2.filename + " at line " + getLineNumber(f2, startIndex2) + " (" + String.format("%.1f", PCofF2) + "% of file length)");
        }
        if (PCofF2 > 20) {
//            System.out.println("Long subsequence found in file " + f1.filename + " at line " + getLineNumber(f1, startIndex1) + " (" + String.format("%.1f", PCofF1) + "% of file length) and in file " + f2.filename + " at line " + getLineNumber(f2, startIndex2) + " (" + String.format("%.1f", PCofF2) + "% of file length)");
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
//        System.out.println("Print String from " + f1.filename + ": " + printsf1);
//        System.out.println("Print String from " + f2.filename + ": " + printsf2);
        printSimilarity = l.apply(printsf1.toLowerCase(), printsf2.toLowerCase());
        similarityPC = percentageScore(printsf1, printsf2, printSimilarity);
//        System.out.println("Distance between " + f1.filename + " and " + f2.filename + ": " + printSimilarity);
//        System.out.println("Percentage Similarity: " + String.format("%.1f", similarityPC) + "%");
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
//        System.out.println("\nNumber of Lines: " + numberofLines + "\nWhitespace Flags: ");

        for (int i = 0; i < fas1.length(); i++) {
            int j = 1;
            while ((i + j) < fas1.length() && fas1.charAt(i) == '\n' && fas1.charAt(i + j) == '\n') {
                j++;
                blankLines++;
//                System.out.println("Blank Lines: " + blankLines + " (char before: " + fas1.charAt(i - 1) + ", " + (i - 1) + ")");
            }
            if (j > 1) {
                over2BlankLinesFlag++;
                over2BlankLinesIndices.add(i);
            }
            i += (j - 1);
        }
//        System.out.println("Blank Lines: " + blankLines + ", Over 2 Blank Lines Flag: " + over2BlankLinesFlag);

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
