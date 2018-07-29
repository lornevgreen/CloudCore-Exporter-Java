package com.cloudcore.exporter.utils;

import com.cloudcore.exporter.core.CloudCoin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class FileUtils {

    /**
     * Attempts to read a JSON Object from a file.
     *
     * @param jsonFilePath the filepath pointing to the JSON file
     * @return String
     */
    public static String loadJSON(String jsonFilePath) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            String line;
            br = new BufferedReader(new FileReader(jsonFilePath));
            while ((line = br.readLine()) != null) {
                jsonData += line + System.lineSeparator();
            }
        } catch (IOException e) {
            System.out.println("Failed to open " + jsonFilePath);
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e){
                //e.printStackTrace();
            }
        }
        return jsonData;
    }

    /** Attempt to read an array of CloudCoins from a JSON String. */
    public static ArrayList<CloudCoin> loadCloudCoinsFromJSON(String fileName) {
        ArrayList<CloudCoin> cloudCoins = new ArrayList<>();

        String fileJson;
        fileJson = loadJSON(fileName);
        if (fileJson == null) {
            System.out.println("File " + fileName + " was not imported.");
            return cloudCoins;
        }

        JSONArray incomeJsonArray;
        try {
            JSONObject json = new JSONObject(fileJson);
            incomeJsonArray = (json.has("cloudcoin")) ? json.getJSONArray("cloudcoin") : new JSONArray().put(json);
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);
                int nn = childJSONObject.getInt("nn");
                int sn = childJSONObject.getInt("sn");
                JSONArray an = childJSONObject.getJSONArray("an");
                ArrayList<String> ans = toStringArrayList(an);
                String ed = childJSONObject.getString("ed");
                String pown = childJSONObject.optString("pown", "");
                String aoidKey = (childJSONObject.has("aoid"))? "aoid" : "aoidText";
                ArrayList<String> aoid = toStringArrayList(childJSONObject.getJSONArray(aoidKey));

                String extension = fileName.substring(fileName.lastIndexOf('.'));
                String currentFilename = fileName.substring(fileName.lastIndexOf(File.separatorChar) + 1,
                        fileName.length() - extension.length());
                cloudCoins.add(new CloudCoin(currentFilename, extension, nn, sn, ans, ed, pown, aoid));
            }
        } catch (JSONException e){
            System.out.println("JSON File " + fileName + " was not imported. " + e.getLocalizedMessage());
            //e.printStackTrace();
        }

        return cloudCoins;
    }

    /**
     * Returns an array containing all filenames in a directory.
     *
     * @param directoryPath the directory to check for files
     * @return String[]
     */
    public static String[] selectFileNamesInFolder(String directoryPath) {
        File dir = new File(directoryPath);
        Collection<String> files = new ArrayList<String>();
        if (dir.isDirectory()) {
            File[] listFiles = dir.listFiles();

            for (File file : listFiles) {
                if (file.isFile()) {//Only add files with the matching file extension
                    files.add(file.getName());
                }
            }
        }
        return files.toArray(new String[]{});
    }

    /**
     * Converts a JSONArray to a String ArrayList
     *
     * @param jsonArray a JSONArray Object
     * @return String[]
     */
    public static ArrayList<String> toStringArrayList(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        ArrayList<String> arr = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++)
            arr.add(jsonArray.optString(i));
        return arr;
    }
}
