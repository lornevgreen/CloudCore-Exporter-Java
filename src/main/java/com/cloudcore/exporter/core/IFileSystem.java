package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.FileUtils;
import com.cloudcore.exporter.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class IFileSystem {


    /* Fields */

    public String RootPath;

    public String ExportFolder;
    public String BankFolder;
    public String FrackedFolder;
    public String TemplateFolder;

    public String LogsFolder;

    public static ArrayList<CloudCoin> bankCoins;
    public static ArrayList<CloudCoin> frackedCoins;


    /* Methods */

    /**
     * Load all CloudCoins in a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (int i = 0, length = filenames.length; i < length; i++) {
            int index = filenames[i].lastIndexOf('.');
            if (index == -1) continue;

            extension = filenames[i].substring(index + 1);
            String fullFilePath = folder + filenames[i];

            switch (extension) {
                //case "celeb":
                //case "celebrium":
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(fullFilePath);
                    folderCoins.addAll(coins);
                    break;
                case "jpg":
                case "jpeg":
                    CloudCoin coin = importJPG(fullFilePath);
                    folderCoins.add(coin);
                    break;
                case "csv":
                    ArrayList<String> lines;
                    try {
                        ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                        lines = new ArrayList<>(Files.readAllLines(Paths.get(fullFilePath)));
                        for (String line : lines)
                            csvCoins.add(CloudCoin.fromCsv(line, filenames[i]));
                        csvCoins.remove(null);
                        folderCoins.addAll(csvCoins);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

        return folderCoins;
    }

    /**
     * Import a CloudCoin embedded in a jpg header.
     *
     * @param fullFilePath the absolute filepath of the JPG file.
     */
    private CloudCoin importJPG(String fullFilePath) {
        // read until Read method returns 0 (end of the stream has been reached)
        try {
            byte[] headerBytes = new byte[455];
            int count, sum = 0;

            FileInputStream inputStream = new FileInputStream(new File(fullFilePath));
            while ((count = inputStream.read(headerBytes, sum, 455 - sum)) > 0) {
                sum += count; // sum is a buffer offset for next reading
            }
            inputStream.close();

            String header = bytesToHexString(headerBytes);
            CloudCoin tempCoin = CloudCoin.fromJpgHeader(header, fullFilePath.substring(fullFilePath.lastIndexOf(File.separatorChar)));
            writeTo(BankFolder, tempCoin);
            return tempCoin;
        } catch (IOException e) {
            System.out.println("IO Exception:" + fullFilePath + e);
            e.printStackTrace();
            return null;
        }
    }

    // TODO: Remove manual JSON code.
    public String setJSON(CloudCoin cc) {
        final String quote = "\"";
        final String tab = "\t";
        String json = (tab + tab + "{ " + System.lineSeparator());// {
        json += tab + tab + quote + "nn" + quote + ":" + quote + cc.nn + quote + ", " + System.lineSeparator();// "nn":"1",
        json += tab + tab + quote + "sn" + quote + ":" + quote + cc.getSn() + quote + ", " + System.lineSeparator();// "sn":"367544",
        json += tab + tab + quote + "an" + quote + ": [" + quote;// "an": ["
        for (int i = 0; (i < 25); i++) {
            json += cc.an.get(i);// 8551995a45457754aaaa44
            if (i == 4 || i == 9 || i == 14 || i == 19) {
                json += quote + "," + System.lineSeparator() + tab + tab + tab + quote; //",
            } else if (i == 24) {
                // json += "\""; last one do nothing
            } else {
                json += quote + ", " + quote;
            }


        }

        json += quote + "]," + System.lineSeparator();//"],

        //CoinUtils cu = new CoinUtils(cc);
        //cu.calcExpirationDate();
        CoinUtils.calcExpirationDate(cc);
        json += tab + tab + quote + "ed" + quote + ":" + quote + cc.ed + quote + "," + System.lineSeparator(); // "ed":"9-2016",
        if (cc.pown == null || cc.pown.length() == 0) {
            cc.pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
        }//Set pown to unknow if it is not set.
        json += tab + tab + quote + "pown" + quote + ":" + quote + cc.pown + quote + "," + System.lineSeparator();// "pown":"uuupppppffpppppfuuf",
        json += tab + tab + quote + "aoid" + quote + ": []" + System.lineSeparator();
        json += tab + tab + "}" + System.lineSeparator();
        // Keep expiration date when saving (not a truley accurate but good enought )
        return json;
    }

    /**
     * Deletes CloudCoins files from a specific folder.
     *
     * @param cloudCoins the ArrayList of CloudCoins to delete.
     * @param folder     the folder to delete from.
     */
    public void RemoveCoins(ArrayList<CloudCoin> cloudCoins, String folder) {
        for (CloudCoin coin : cloudCoins) {
            try {
                Files.deleteIfExists(Paths.get(folder + coin.getFullFilePath()));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes an array of CloudCoins to a single Stack file.
     *
     * @param coins    the ArrayList of CloudCoins.
     * @param filePath the absolute filepath of the CloudCoin file, without the extension.
     */
    public void WriteCoinsToSingleStack(ArrayList<CloudCoin> coins, String filePath) {
        Gson gson = Utils.createGson();
        try {
            Stack stack = new Stack(coins.toArray(new CloudCoin[0]));
            Files.write(Paths.get(filePath + ".stack"), gson.toJson(stack).getBytes());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Writes a CloudCoins a Stack file.
     *
     * @param coin     the ArrayList of CloudCoins.
     * @param filePath the absolute filepath of the CloudCoin file, without the extension.
     */
    public void WriteCoinToIndividualStacks(CloudCoin coin, String filePath) {
        Stack stack = new Stack(coin);
        try {
            Files.write(Paths.get(filePath + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Writes a CloudCoin object to a JPG image. The CloudCoin is embedded in the JPG header as defined here:
     * <a href="http://www.cloudcoinwiki.com/File_Formats#JPEG_File_Format_for_CloudCoins">JPEG File Format</a>.
     *
     * @param cloudCoin    a CloudCoin object.
     * @param TemplateFile a template JPG image.
     * @param filePath     the absolute filepath of the new CloudCoin-embedded JPG, without the extension.
     * @return
     */
    public abstract boolean writeCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String filePath);

    /**
     * Writes an array of CloudCoin objects to a CSV file.
     *
     * @param cloudCoins the ArrayList of CloudCoin objects.
     * @param filePath   the absolute filepath of the new CloudCoin-embedded JPG, without the extension.
     * @return true if the file was saved to the location provided, otherwise false.
     */
    public abstract boolean writeCoinsToCsv(ArrayList<CloudCoin> cloudCoins, String filePath);

    /**
     * Returns the full file path for a JPG image template.
     *
     * @param cloudCoin the CloudCoin that needs a JPG image template.
     * @return the full file path for a JPG image template.
     */
    public String GetCoinTemplate(CloudCoin cloudCoin) {
        int denomination = CoinUtils.getDenomination(cloudCoin);

        if (denomination == 0)
            return null;
        else
            return TemplateFolder + "jpeg" + denomination + ".jpg";
    }

    /**
     * Converts a byte array to a hexadecimal String.
     *
     * @param data the byte array.
     * @return a hexadecimal String.
     */
    public String bytesToHexString(byte[] data) {
        final String HexChart = "0123456789ABCDEF";
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(HexChart.charAt((b & 0xF0) >> 4)).append(HexChart.charAt((b & 0x0F)));
        return hex.toString();
    }

    // TODO: Replace boilerplate code for writing a CloudCoin to a file.
    public boolean writeTo(String folder, CloudCoin cc) {
        //CoinUtils cu = new CoinUtils(cc);
        final String quote = "\"";
        final String tab = "\t";
        String wholeJson = "{" + System.lineSeparator(); //{
        String filename = CoinUtils.generateFilename(cc);

        String json = this.setJSON(cc);

        try {
            if (!Files.exists(Paths.get(folder + CoinUtils.generateFilename(cc) + ".stack"))) {
                wholeJson += tab + quote + "cloudcoin" + quote + ": [" + System.lineSeparator(); // "cloudcoin" : [
                wholeJson += json;
                wholeJson += System.lineSeparator() + tab + "]" + System.lineSeparator() + "}";
                Files.write(Paths.get(folder + filename + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            } else {
                if (folder.contains("Counterfeit") || folder.contains("Trash")) {
                    return false;
                } else if (folder.contains("Imported")) {
                    Files.delete(Paths.get(folder + filename + ".stack"));
                    Files.write(Paths.get(folder + filename + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
                    return false;
                } else {
                    System.out.println(filename + ".stack" + " already exists in the folder " + folder);
                    return true;
                }
            }
            // TODO: Should not write text twice
            Files.write(Paths.get(folder + filename + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
