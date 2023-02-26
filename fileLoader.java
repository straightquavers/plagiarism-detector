import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class fileLoader {

    // figure out how to not be caught for plagiarism for bufferedreader
    // figure out how to tokenise files into strings - probably scanner

    private static File directory;
    private ArrayList<String> filenames;
    File[] files;
    ArrayList<File> filelist;

    private static final int BUFFER_SIZE = 4096;

    // constructor that takes file directory
    public fileLoader(File d) {
        // read other zip filenames from directory
        directory = d;
        String[] tempFilenames = directory.list();

        File tempDirectory = new File(directory + "\\temp");
        if (tempDirectory.exists()) {
            tempDirectory.delete();
        }
        tempDirectory.mkdirs();

        filenames = new ArrayList<String>();

        for (int i = 0; i < tempFilenames.length; i++) {
            if (getExt(tempFilenames[i]).equals("zip")) {
                filenames.add(tempFilenames[i]);
            }
        }

        for (int i = 0; i < filenames.size(); i++) {
            File f = new File(directory, filenames.get(i));
            readToString(f, tempDirectory);
        }

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

                for (int i = 0; i < srcFiles.length; i++) {
                    if (!srcFiles[i].isDirectory()) {
                        if (getExt(srcFiles[i].getAbsolutePath()).equals("java")) {
                            filelist.add(srcFiles[i]);
                        }
                    } else {
                        File currentPackage = new File(srcFiles[i].getName());
                        currentPackage.mkdirs();
                    }
                }
            
        } catch (IOException e) {
            System.out.println("An error has occurred. Please try again with a valid directory.");
            e.printStackTrace();
        }

        for (File file : filelist) {
            System.out.println(file.getName() + ": " + file.getParent());
        }
        // directory.delete();
    }

    public void extract(String filepath, String destination) throws IOException {
        ZipInputStream zi = new ZipInputStream(new FileInputStream(filepath));
        ZipEntry ze = zi.getNextEntry();
        while (ze != null) {
            String newFilepath = destination + "\\" + ze.getName();
            if (!ze.isDirectory()) {
                // only if files are java and in source
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFilepath));
                byte[] bytesIn = new byte[BUFFER_SIZE];
                int read = 0;
                while ((read = zi.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
            } else {
                File newDirectory = new File(newFilepath);
                newDirectory.mkdirs();
            }
            zi.closeEntry();
            ze = zi.getNextEntry();
        }
        zi.close();

        // delete files that aren't src or java
    }
}
