package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import com.cloudcore.exporter.core.Stack;
import com.google.gson.JsonSyntaxException;

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
    public static String ensureFilenameUnique(String filename, String extension, String folder) {
        if (!Files.exists(Paths.get(folder + filename + extension)))
            return filename + extension;

        filename = filename + '.';
        String newFilename;
        int loopCount = 0;
        do {
            newFilename = filename + Integer.toString(++loopCount);
        }
        while (Files.exists(Paths.get(folder + newFilename + extension)));
        return newFilename + extension;
    }

    /**
     * Loads an array of CloudCoins from a Stack file.
     *
     * @param folder   the folder containing the Stack file.
     * @param filename the absolute filepath of the Stack file.
     * @return ArrayList of CloudCoins.
     */
    public static ArrayList<CloudCoin> loadCloudCoinsFromStack(String folder, String filename) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(folder + filename)));
            Stack stack = Utils.createGson().fromJson(file, Stack.class);
            for (CloudCoin coin : stack.cc) {
                coin.folder = folder;
                coin.currentFilename = filename;
            }
            return new ArrayList<>(Arrays.asList(stack.cc));
        } catch (IOException | JsonSyntaxException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
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

            if (null != filenames) {
                for (File file : filenames) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }
        return files.toArray(new String[]{});
    }
}
