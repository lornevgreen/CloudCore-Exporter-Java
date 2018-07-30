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

    public String RootPath;

    public String ImportFolder;
    public String ExportFolder;
    public String BankFolder;
    public String FrackedFolder;
    public String TemplateFolder;
    public String CSVFolder;

    public String LogsFolder;

    public static ArrayList<CloudCoin> bankCoins;
    public static ArrayList<CloudCoin> frackedCoins;

    public ArrayList<CloudCoin> LoadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (int i = 0, fileNamesLength = fileNames.length; i < fileNamesLength; i++) {
            String fileName = fileNames[i];
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);
                String filepath = folder + fileName;

                switch (extension) {
                    case "celeb":
                    case "celebrium":
                    case "stack":
                        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(filepath);
                        folderCoins.addAll(coins);
                        break;
                    case "jpg":
                    case "jpeg":
                        CloudCoin coin = importJPEG(filepath);
                        folderCoins.add(coin);
                        break;
                    case "csv":
                        ArrayList<String> lines;
                        try {
                            ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                            lines = new ArrayList<>(Files.readAllLines(Paths.get(filepath)));
                            for (String line : lines) {
                                csvCoins.add(CloudCoin.fromCsv(line, fileName));
                            }
                            csvCoins.remove(null);
                            folderCoins.addAll(csvCoins);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }

        return folderCoins;
    }

    /**
     * Import a CloudCoin embedded in a jpg header.
     */
    private CloudCoin importJPEG(String fileName) {
        try {
            byte[] headerBytes = new byte[455];
            String header = "";
            int count, sum = 0;

            FileInputStream inputStream = new FileInputStream(new File(fileName));
            // read until Read method returns 0 (end of the stream has been reached)
            while ((count = inputStream.read(headerBytes, sum, 455 - sum)) > 0) {
                sum += count; // sum is a buffer offset for next reading
            }
            inputStream.close();

            header = bytesToHexString(headerBytes);
            CloudCoin tempCoin = CloudCoin.fromJpgHeader(header, fileName.substring(fileName.lastIndexOf(File.separatorChar)));
            writeTo(BankFolder, tempCoin);
            return tempCoin;
        } catch (IOException e) {
            System.out.println("IO Exception:" + fileName + e);
            e.printStackTrace();
        }
        return null;
    }

    // end json test
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


    public void RemoveCoins(ArrayList<CloudCoin> coins, String folder) {
        for (CloudCoin coin : coins) {
            try {
                System.out.println("deleting: " + folder + coin.currentFilename + coin.currentExtension);
                Files.deleteIfExists(Paths.get(folder + coin.currentFilename + coin.currentExtension));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void WriteCoinsToStack(ArrayList<CloudCoin> coins, String fileName) {
        Gson gson = Utils.createGson();
        try {
            Stack stack = new Stack(coins.toArray(new CloudCoin[0]));
            Files.write(Paths.get(fileName + ".stack"), gson.toJson(stack).getBytes());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void WriteCoinToStack(CloudCoin coin, String filename) {
        Stack stack = new Stack(coin);
        try {
            Files.write(Paths.get(filename + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String OutputFile);

    public abstract boolean WriteCoinToCsv(ArrayList<CloudCoin> exportCoins, String filename);

    public String GetCoinTemplate(CloudCoin cloudCoin) {
        int denomination = CoinUtils.getDenomination(cloudCoin);

        if (denomination == 0)
            return null;
        else
            return TemplateFolder + "jpeg" + denomination + ".jpg";
    }

    public String bytesToHexString(byte[] data) {
        final String HexChart = "0123456789ABCDEF";
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(HexChart.charAt((b & 0xF0) >> 4)).append(HexChart.charAt((b & 0x0F)));
        return hex.toString();
    }

    public boolean writeTo(String folder, CloudCoin cc) {
        //CoinUtils cu = new CoinUtils(cc);
        final String quote = "\"";
        final String tab = "\t";
        String wholeJson = "{" + System.lineSeparator(); //{
        String filename = CoinUtils.getFilename(cc);

        String json = this.setJSON(cc);

        try {
            if (!Files.exists(Paths.get(folder + CoinUtils.getFilename(cc) + ".stack"))) {
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

    }//End Write To
}
