import java.util.ArrayList;

public class collusionFile {
    String filename;
    String fileAsString;
    ArrayList<String> tokenizedFile;
    ArrayList<String> tokenFileNoKeywords;
    String commentsAsString;
    ArrayList<String> tokenizedComments;
    ArrayList<String> fileByLine;

    public int over2BlankLinesFlag;
    public int over2BlankLinesPC;
    public ArrayList<Integer> over2BlankLinesIndices;
    public int endOfLineSpacesFlag;
    public int endSpacesPC;
    public ArrayList<Integer> endSpacesIndices;
    public int startOfLineSpacesFlag;
    public int startSpacesPC;
    public ArrayList<Integer> startSpacesIndices;
    ArrayList<Integer> newLines;
    public int blankLines;

    int endOfLineFlag;
    int startOfLineFlag;
    int ownLineFlag;
    ArrayList<Integer> formattingIndices;
    ArrayList<Integer> endOfLineBracket;
    ArrayList<Integer> startOfLineBracket;
    ArrayList<Integer> ownLineBracket;
    String formattingIndicator;

    public collusionFile(String fn, String fas, ArrayList<String> tf, ArrayList<String> tfnk, String cas, ArrayList<String> tc) {
        filename = fn;
        fileAsString = fas;
        tokenizedFile = tf;
        tokenFileNoKeywords = tfnk;
        commentsAsString = cas;
        tokenizedComments = tc;

        over2BlankLinesIndices = new ArrayList<Integer>();
        endSpacesIndices = new ArrayList<Integer>();
        startSpacesIndices = new ArrayList<Integer>();
        newLines = new ArrayList<Integer>();
        fileByLine = new ArrayList<String>();

        formattingIndices = new ArrayList<Integer>();
        endOfLineBracket = new ArrayList<Integer>();
        startOfLineBracket = new ArrayList<Integer>();
        ownLineBracket = new ArrayList<Integer>();
        formattingIndicator = "";
    }

}
