import java.util.ArrayList;

public class scoreCalculator {

    collusionFile f1;
    collusionFile f2;
    double blankLineResultsScore;
    double startSpacesResultsScore;
    double endSpacesResultsScore;
    double startBracketsResultsScore;
    double endBracketResultsScore;
    double ownBracketResultsScore;

    double fasSimilarity;
    double commentSimilarity;

    double tokenSimilarity;
    double commentTokenSimilarity;

    double outputSimilarity;
    double substringSimilarity;
    double finalScore;
    //stores tolerances
    //calculates final percentage for file
    //takes numbers from collision checker and calculates score

//    - Whitespace - checks if over 2 blank lines, then compares those flags to other files to find substring (assuming 4 spaces is the typical java indentation [https://www.oracle.com/java/technologies/javase/codeconventions-indentation.html#:~:text=Four spaces should be used,8 spaces (not 4)](https://www.oracle.com/java/technologies/javase/codeconventions-indentation.html#:~:text=Four%20spaces%20should%20be%20used,8%20spaces%20(not%204)))
//            - Formatting - checks if there are 15%-50% the number of { total, then compares flags to other files to find substring.
//        - Filename matching
//                - Levenshtein distance for whole file
//        - Levenshtein distance for comments
//                - Levenshtein distance for each line > 50% similar
//                - Levenshtein distance for substrings > 20% of file length
//        - Identifies tokens (no punctuation or keywords) > 80% similar
//                - Identifies comment tokens (no punctuation) > 80% similar
//                - Output printlns

    public scoreCalculator(collusionFile _f1, collusionFile _f2) {
        collisionChecker cc = new collisionChecker(_f1, _f2);

        f1 = _f1;
        f2 = _f2;

        //weight high - 50%
        int usernameMatch;
        if (cc.usernameMatch) {
            usernameMatch = 1;
        } else {
            usernameMatch = 0;
        }

        blankLineResultsScore = cc.blankLineResultsScore * 0.003;
        startSpacesResultsScore = cc.startSpacesResultsScore * 0.003;
        endSpacesResultsScore = cc.endSpacesResultsScore * 0.001;
        startBracketsResultsScore = cc.startBracketsResultsScore * 0.003;
        endBracketResultsScore = cc.endBracketResultsScore * 0.003;
        ownBracketResultsScore = cc.ownBracketResultsScore * 0.003;

        fasSimilarity = cc.fasSimilarity * 0.4;
        commentSimilarity = cc.commentSimilarity * 0.4;

        tokenSimilarity = cc.tokenScore * 0.1;
        commentTokenSimilarity = cc.commentTokenScore * 0.1;

        outputSimilarity = cc.similarityPC * 0.2;
        substringSimilarity = cc.substringScore * 0.2;


        finalScore = 80*(blankLineResultsScore + startSpacesResultsScore + endSpacesResultsScore + startBracketsResultsScore + endBracketResultsScore + ownBracketResultsScore + fasSimilarity + commentSimilarity + tokenSimilarity + commentTokenSimilarity + outputSimilarity + substringSimilarity);

        if (finalScore > 100) {
            finalScore = 100;
        }

        //flags modifier - score as a percentage of 20
        //strings indices 1, 2 ->
        // list of all flags ->
        // flag index f1, flag index f2, start index f1, end index f1
        // value to check - string length * number of strings found

    }

    public double getScore() {
        return finalScore;
    }

    public String getNames() {
        String names = f1.filename + " & " + f2.filename;
        return names;
    }

    //longest common subsequence - adds similarity for the percentage of the length of the file that the subsequence takes up

    //if over 40% of the tokens are similar, raise flag
}
