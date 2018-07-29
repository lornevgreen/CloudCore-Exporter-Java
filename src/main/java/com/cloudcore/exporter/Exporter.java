package com.cloudcore.exporter;

import com.cloudcore.exporter.core.*;
import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.SimpleLogger;
import com.cloudcore.exporter.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class Exporter {

    /* INSTANCE VARIABLES */
    IFileSystem fileSystem;
    public SimpleLogger logger;

    public static int onesCount = 0;
    public static int fivesCount = 0;
    public static int qtrCount = 0;
    public static int hundredsCount = 0;
    public static int twoFiftiesCount = 0;

    public static int onesFrackedCount = 0;
    public static int fivesFrackedCount = 0;
    public static int qtrFrackedCount = 0;
    public static int hundredsFrackedCount = 0;
    public static int twoFiftiesFrackedCount = 0;

    public static int onesTotalCount = 0;
    public static int fivesTotalCount = 0;
    public static int qtrTotalCount = 0;
    public static int hundredsTotalCount = 0;
    public static int twoFiftiesTotalCount = 0;


    /* CONSTRUCTOR */

    public Exporter(IFileSystem fileUtils) {
        this.fileSystem = fileUtils;
    }


    /* PUBLIC METHODS */

    public void CalculateTotals() {
        // Count all Bank coins
        ArrayList<CloudCoin> bankCoins = fileSystem.LoadFolderCoins(fileSystem.BankFolder);
        for (CloudCoin coin : bankCoins) {
            int denomination = CoinUtils.getDenomination(coin);

            if (denomination == 1) onesCount++;
            else if (denomination == 5) fivesCount++;
            else if (denomination == 25) qtrCount++;
            else if (denomination == 100) hundredsCount++;
            else if (denomination == 250) twoFiftiesCount++;
        }

        // Count all Fracked coins
        ArrayList<CloudCoin> frackedCoins = fileSystem.LoadFolderCoins(fileSystem.FrackedFolder);
        for (CloudCoin coin : frackedCoins) {
            int denomination = CoinUtils.getDenomination(coin);

            if (denomination == 1) onesFrackedCount++;
            else if (denomination == 5) fivesFrackedCount++;
            else if (denomination == 25) qtrFrackedCount++;
            else if (denomination == 100) hundredsFrackedCount++;
            else if (denomination == 250) twoFiftiesFrackedCount++;
        }
        bankCoins.addAll(frackedCoins);

        // Count all valid coins
        onesTotalCount = onesCount + onesFrackedCount;
        fivesTotalCount = fivesCount + fivesFrackedCount;
        qtrTotalCount = qtrCount + qtrFrackedCount;
        hundredsTotalCount = hundredsCount + hundredsFrackedCount;
        twoFiftiesTotalCount = twoFiftiesCount + twoFiftiesFrackedCount;
    }

    public void ExportCoins() {
        CalculateTotals();
        Scanner reader = new Scanner(System.in);
        String stackType = "1";

        System.out.println("Do you want to export your CloudCoin to (1)jpgs, (2) stack (JSON), or (3) CSV file?");
        String fileType = reader.next();
        if ("2".equals(fileType)) {
            System.out.println("Export All Coins to Single Stack (1) or One Stack per coin (2)?");
            stackType = reader.next();
        }

        int exp_1 = 0;
        int exp_5 = 0;
        int exp_25 = 0;
        int exp_100 = 0;
        int exp_250 = 0;

        if (onesTotalCount > 0) {
            System.out.println("How many 1s do you want to export?");
            exp_1 = Math.min(Utils.tryParseInteger(reader.next()), onesTotalCount);
        }
        if (fivesTotalCount > 0) {
            System.out.println("How many 5s do you want to export?");
            exp_5 = Math.min(Utils.tryParseInteger(reader.next()), fivesTotalCount);
        }
        if ((qtrTotalCount > 0)) {
            System.out.println("How many 25s do you want to export?");
            exp_25 = Math.min(Utils.tryParseInteger(reader.next()), qtrTotalCount);
        }
        if (hundredsTotalCount > 0) {
            System.out.println("How many 100s do you want to export?");
            exp_100 = Math.min(Utils.tryParseInteger(reader.next()), hundredsTotalCount);
        }
        if (twoFiftiesTotalCount > 0) {
            System.out.println("How many 250s do you want to export?");
            exp_250 = Math.min(Utils.tryParseInteger(reader.next()), twoFiftiesTotalCount);
        }

        System.out.println("What tag will you add to the file name?");
        String tag = null;
        while (reader.hasNextLine()) {
            if (tag == null) {
                tag = "";
                reader.nextLine();
            } else {
                tag = reader.nextLine();
                if (tag.length() != 0)
                    tag = '.' + tag;
                break;
            }
        }

        int totalSaved = exp_1 + (exp_5 * 5) + (exp_25 * 25) + (exp_100 * 100) + (exp_250 * 250);
        ArrayList<CloudCoin> totalCoins = IFileSystem.bankCoins;
        totalCoins.addAll(IFileSystem.frackedCoins);

        ArrayList<CloudCoin> onesToExport = new ArrayList<>();
        ArrayList<CloudCoin> fivesToExport = new ArrayList<>();
        ArrayList<CloudCoin> qtrToExport = new ArrayList<>();
        ArrayList<CloudCoin> hundredsToExport = new ArrayList<>();
        ArrayList<CloudCoin> twoFiftiesToExport = new ArrayList<>();

        for (int i = 0, totalCoinsSize = totalCoins.size(); i < totalCoinsSize; i++) {
            CloudCoin coin = totalCoins.get(i);
            int denomination = CoinUtils.getDenomination(coin);

            if (denomination == 1) {
                if (exp_1-- > 0) onesToExport.add(coin);
                else exp_1 = 0;
            } else if (denomination == 5) {
                if (exp_5-- > 0) fivesToExport.add(coin);
                else exp_5 = 0;
            } else if (denomination == 25) {
                if (exp_25-- > 0) qtrToExport.add(coin);
                else exp_25 = 0;
            } else if (denomination == 100) {
                if (exp_100-- > 0) hundredsToExport.add(coin);
                else exp_100 = 0;
            } else if (denomination == 250) {
                if (exp_250-- > 0) twoFiftiesToExport.add(coin);
                else exp_250 = 0;
            }
        }

        ArrayList<CloudCoin> exportCoins = onesToExport;
        exportCoins.addAll(fivesToExport);
        exportCoins.addAll(qtrToExport);
        exportCoins.addAll(hundredsToExport);
        exportCoins.addAll(twoFiftiesToExport);

        String filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag + "");
        if (Files.exists(Paths.get(filename))) // tack on a random number if a file already exists with the same tag
        {
            Random rnd = new Random();
            int tagrand = rnd.nextInt(999);
            filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag + tagrand + "");
            // TODO: ACTUALLY RENAME THE FILE
        }

        // Export Coins as jpeg Images
        if ("1".equals(fileType)) {
            for (CloudCoin coin : exportCoins) {
                String OutputFile = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag + ".jpg";
                boolean fileGenerated = fileSystem.WriteCoinToJpeg(coin, fileSystem.GetCoinTemplate(coin), OutputFile);
                if (fileGenerated)
                    updateLog("CloudCoin exported as Jpeg to " + OutputFile);
            }

            //fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
            //fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);
        }

        // Export Coins as Stack
        else if ("2".equals(fileType)) {
            if ("1".equals(stackType)) { // Single Stack, or individual Stacks
                fileSystem.WriteCoinsToFile(exportCoins, filename, ".stack");
                fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
                fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);
            } else {
                for (CloudCoin coin : exportCoins) {
                    System.out.print("filename: " + CoinUtils.getFilename(coin));
                    String OutputFile = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag + ".stack";
                    fileSystem.WriteCoinToFile(coin, OutputFile);
                    fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
                    fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);

                    updateLog("CloudCoin exported as Stack to " + OutputFile);
                }
            }
        }

        // Export Coins as CSV
        else if ("3".equals(fileType)) {
            filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag + ".csv");
            if (Files.exists(Paths.get(filename))) {
                // tack on a random number if a file already exists with the same tag
                Random rnd = new Random();
                int tagrand = rnd.nextInt(999);
                filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag + tagrand + "");
            }

            StringBuilder csv = new StringBuilder();
            StringBuilder header = new StringBuilder();
            String headerLine = "sn,denomination,nn,";

            for (int i = 0; i < Config.NodeCount; i++) {
                header.append("an").append((i + 1)).append(",");
            }

            // Write the Header Record
            csv.append(headerLine).append(header.toString()).append(System.lineSeparator());

            // Write the Coin Serial Numbers
            for (CloudCoin coin : exportCoins) {
                StringBuilder anString = new StringBuilder();
                for (int i = 0; i < Config.NodeCount; i++) {
                    anString.append(coin.an.get(i)).append(",");
                }
                String newLine = String.format("{0},{1},{2},{3}", coin.getSn(), CoinUtils.getDenomination(coin), coin.nn, anString.toString());
                csv.append(newLine).append(System.lineSeparator());
            }
            try {
                Files.write(Paths.get(filename), csv.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }


            //fileSystem.WriteCoinsToFile(exportCoins, filename, ".s");
            fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
            fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);
        }
    }

    public void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }


    /* PRIVATE METHODS */


    /* PRIVATE METHODS */

}
