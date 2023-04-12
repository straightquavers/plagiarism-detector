import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import opennlp.tools.tokenize.SimpleTokenizer;

public class fileLoader {

    private static File directory;
    private ArrayList<String> filenames;
    File[] files;
    ArrayList<File> filelist;
    ArrayList<String> filesAsStrings;
    ArrayList<ArrayList<String>> tokenizedFiles;
    ArrayList<ArrayList<String>> tokenFilesNoKeywords;
    ArrayList<String> commentsAsStrings;
    ArrayList<ArrayList<String>> tokenizedComments;
    String givenCode;
    ArrayList<collusionFile> collusionFiles;

    static String keywords[] = {"abstract", "assert", "args", "boolean", "break", "byte", "case", "catch", "char", "class", "continue", "default", "do", "double", "else", "extends", "false", "final", "finally", "float", "for", "i", "if", "implements", "import", "instanceof", "int", "interface", "long", "main", "native", "new", "null", "out", "package", "private", "protected", "public", "print", "println", "return", "short", "static", "strictfp", "String", "super", "switch", "synchronized", "System", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while"};

    // constructor that takes file directory
    public fileLoader(File d, String _givenCode) {
        givenCode = _givenCode;
        collusionFiles = new ArrayList<collusionFile>();
        // read other zip filenames from directory
        directory = d;
        String[] tempFilenames = directory.list();

        File tempDirectory = new File(directory + "\\temp");
        if (tempDirectory.exists()) {
            tempDirectory.delete();
        }
        tempDirectory.mkdirs();

        filenames = new ArrayList<String>();
        filesAsStrings = new ArrayList<String>();
        tokenizedFiles = new ArrayList<ArrayList<String>>();
        commentsAsStrings = new ArrayList<String>();
        tokenizedComments = new ArrayList<ArrayList<String>>();
        tokenFilesNoKeywords = new ArrayList<ArrayList<String>>();

        for (int i = 0; i < tempFilenames.length; i++) {
            if (getExt(tempFilenames[i]).equals("zip")) {
                filenames.add(tempFilenames[i]);
            }
        }

        for (int i = 0; i < filenames.size(); i++) {
            File f = new File(directory, filenames.get(i));
            readToString(f, tempDirectory);
        }

        for (String a : filesAsStrings) {
            tokenizedFiles.add(tokenize(a));
        }

        for (String a : commentsAsStrings) {
            tokenizedComments.add(tokenize(a));
        }

        for (ArrayList<String> a : tokenizedFiles) {
            ArrayList<String> noKeywords = new ArrayList<String>();
            boolean dotFlag = false;
            keywordLoop:
            for (String t : a) {
                if (!isKeyword(t) && !noKeywords.contains(t) && !Character.isDigit(t.charAt(0))) {
                    if (dotFlag) {
                        dotFlag = false;
                        continue keywordLoop;
                    }
                    noKeywords.add(t);
                }
                if (t.charAt(0) == '.') {
                    dotFlag = true;
                }
            }

            tokenFilesNoKeywords.add(noKeywords);
        }

        System.out.println(filenames.size() + " files found." + "\n\nHere are the filenames:");
        for (int i = 0; i < filenames.size(); i++) {
            System.out.println(filenames.get(i));
        }

//        System.out.println("\n\nHere are the files as strings:");
//        for (int i = 0; i < filesAsStrings.size(); i++) {
//            System.out.println("\n" + filenames.get(i) + ":\n" + filesAsStrings.get(i) + "\n///////////////////////////////////////////////");
//        }
//
//        System.out.println("\nHere are the comments as strings:");
//        for (int i = 0; i < commentsAsStrings.size(); i++) {
//            System.out.println(commentsAsStrings.get(i));
//        }
//
//        System.out.println("\n\nHere are the tokenized files:");
//        for (int i = 0; i < tokenizedFiles.size(); i++) {
//            System.out.println(tokenizedFiles.get(i));
//        }
//
//        System.out.println("\n\nHere are the tokenized comments:");
//        for (int i = 0; i < tokenizedComments.size(); i++) {
//            System.out.println(tokenizedComments.get(i));
//        }
//
//        System.out.println("\n\nHere are the tokenized files, with no comments, keywords, or punctuation:");
//        for (int i = 0; i < tokenFilesNoKeywords.size(); i++) {
//            System.out.println(tokenFilesNoKeywords.get(i));
//        }

        for (int i = 0; i < filenames.size(); i++) {
            collusionFiles.add(new collusionFile(filenames.get(i), filesAsStrings.get(i), tokenizedFiles.get(i), tokenFilesNoKeywords.get(i), commentsAsStrings.get(i), tokenizedComments.get(i)));
        }
    }

    public boolean isKeyword(String token) {
        for (int i = 0; i < keywords.length; i++) {
            if (token.equals(keywords[i])) {
                return true;
            } else if (token.matches("\\p{Punct}{1,}")) {
                return true;
            }
        }
        return false;
    }

    public String getExt(String filepath) {
        String extension = "";
        File zipFile = new File(filepath);
        String absoluteFilepath = zipFile.getAbsolutePath();
        String[] parts = absoluteFilepath.split("\\.");
        extension = parts[parts.length - 1];
        return extension;
    }

    public void readToString(File directory, File filepath) {
        try {
            extract(directory.getAbsolutePath(), filepath.getAbsolutePath());
            files = filepath.listFiles();
            filelist = new ArrayList<File>();
            String srcString = directory.getAbsolutePath().replace(".zip", "") + "\\src";
            File currentFile = new File(srcString);
            File[] srcFiles = currentFile.listFiles();
            String fileAsString = "";
            String commentAsString = "";
            boolean lineHasComment;
            boolean lineHasMultiComment;

            for (int i = 0; i < srcFiles.length; i++) {
                if (!srcFiles[i].isDirectory()) {
                    if (getExt(srcFiles[i].getAbsolutePath()).equals("java")) {
                        filelist.add(srcFiles[i]);
                        Scanner reader = new Scanner(srcFiles[i]);
                        String currentFileString = "";
                        String currentCommentString = "";
//                        boolean commentLineFlag = false;
                        while (reader.hasNextLine()) {
                            lineHasComment = false;
                            lineHasMultiComment = false;
                            String currentLine = reader.nextLine();
                            for (int j = 0; j < currentLine.length() - 1; j++) {
                                char thisChar = currentLine.charAt(j);
                                char nextChar = currentLine.charAt(j + 1);
                                if (thisChar == '/' && (nextChar == '/' || nextChar == '*')) {
                                    currentCommentString += "\n";
                                    lineHasComment = true;
                                    currentCommentString += currentLine.substring(j + 2);
                                }
                                if (thisChar == '*' && nextChar == '/') {
                                    currentCommentString += "\n";
                                    lineHasMultiComment = true;
                                    currentCommentString += currentLine.substring(0, j);
                                }
                            }
                            if (lineHasComment) {
//                                if (!commentLineFlag && currentFileString.charAt(currentFileString.length() - 1) == '\n') {
//                                    currentFileString += "\n";
//                                    commentLineFlag = true;
//                                } else {
//                                    commentLineFlag = false;
//                                }
                                currentLine = currentLine.split("/")[0].stripTrailing();
                            } else if (lineHasMultiComment) {
                                currentLine = "";
                            } else {
                                currentFileString += "\n";
//                                commentLineFlag = false;
                            }
                            currentFileString += currentLine;
                        }
                        reader.close();
                        fileAsString += currentFileString;
                        commentAsString += currentCommentString;
                    }
                } else {
                    File currentPackage = new File(srcFiles[i].getName());
                    currentPackage.mkdirs();
                }
            }

            int givenCodeIndex = fileAsString.indexOf(givenCode);
            if (!givenCode.isBlank()) {
                while (givenCodeIndex >= 0) {
                    String file1 = fileAsString.substring(0, givenCodeIndex);
                    String file2 = fileAsString.substring(givenCodeIndex + givenCode.length());
                    fileAsString = file1 + file2;
                    givenCodeIndex = fileAsString.indexOf(givenCode);
                }
            }

            filesAsStrings.add(fileAsString);
            commentsAsStrings.add(commentAsString);

        } catch (IOException e) {
            System.out.println("An error has occurred. Please try again with a valid directory.");
            e.printStackTrace();
        }

        filepath.delete();
    }

    public void extract(String filepath, String destination) throws IOException {
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(filepath));
        ZipEntry zipEntry = zipInput.getNextEntry();
        while (zipEntry != null) {
            String newFilepath = destination + "\\" + zipEntry.getName();
            if (!zipEntry.isDirectory()) {
                // only if files are java and in source
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFilepath));
                byte[] buffer = new byte[1024];
                int readIn = 0;
                while ((readIn = zipInput.read(buffer)) != -1) {
                    bos.write(buffer, 0, readIn);
                }
                bos.close();
            } else {
                File newDirectory = new File(newFilepath);
                newDirectory.mkdirs();
            }
            zipInput.closeEntry();
            zipEntry = zipInput.getNextEntry();
        }
        zipInput.close();

        // delete files that aren't src or java
    }

    public ArrayList<String> tokenize(String a) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        ArrayList<String> tokenArray = new ArrayList<String>();
        String tokens[] = tokenizer.tokenize(a);
        List<String> tokensList = Arrays.asList(tokens);
        tokenArray.addAll(tokensList);
        return tokenArray;
    }
}


