package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Stack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FileUtils {


    /* Methods */

    /**
     * Appends a filename with an increasing index if a filename is in use. Loops until a free filename is found.
     * TODO: Potential endless loop if every filename is taken.
     *
     * @param filename
     * @return an unused filename
     */
    public static String ensureFilenameUnique(String filename, String extension) {
        if (!Files.exists(Paths.get(filename + extension)))
            return filename;

        filename = filename + '.';
        String newFilename;
        int loopCount = 0;
        do {
            newFilename = filename + Integer.toString(++loopCount);
        }
        while (Files.exists(Paths.get(newFilename + extension)));
        return newFilename;
    }

    /**
     * Attempts to read a JSON Object from a file.
     *
     * @param fullFilePath the absolute filepath of the JSON file.
     * @return JSON String
     */
    public static String loadJSON(String fullFilePath) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(fullFilePath));
            while ((line = br.readLine()) != null) {
                jsonData += line + System.lineSeparator();
            }
        } catch (IOException e) {
            System.out.println("Failed to open " + fullFilePath);
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        return jsonData;
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param fullFilePath the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String fullFilePath) {
        String fileJson = loadJSON(fullFilePath);
        if (fileJson == null) {
            System.out.println("File " + fullFilePath + " was not imported.");
            return new ArrayList<>();
        }

        try {
            Stack stack = Utils.createGson().fromJson(fileJson, Stack.class);
            for (CloudCoin coin : stack.cc)
                coin.setFullFilePath(fullFilePath);
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param folderPath the folder to check for files.
     * @return String Array.
     */
    public static String[] selectFileNamesInFolder(String folderPath) {
        File folder = new File(folderPath);
        Collection<String> files = new ArrayList<>();
        if (folder.isDirectory()) {
            File[] filenames = folder.listFiles();

            for (File file : filenames) {
                if (file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }
}
