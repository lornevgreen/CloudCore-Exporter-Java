package com.cloudcore.exporter;

import com.cloudcore.exporter.core.*;
import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.FileUtils;
import com.cloudcore.exporter.utils.SimpleLogger;
import com.cloudcore.exporter.utils.Utils;

import java.util.ArrayList;
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

        // Ask for export type.
        System.out.println("Do you want to export your CloudCoin to (1) stack (JSON), (2) jpgs, or (3) CSV file?");
        String fileType = reader.next();

        // Ask for export subtype for stacks.
        if ("1".equals(fileType)) {
            System.out.println("Export All Coins to Single Stack (1) or One Stack per coin (2)?");
            stackType = reader.next();
        }

        // Ask for amounts to export.
        int exp_1 = 0, exp_5 = 0, exp_25 = 0, exp_100 = 0, exp_250 = 0;
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

        // Ask for an optional tag to be appended in the filename.
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

        // Get the CloudCoins that will be used for the export.
        int totalSaved = exp_1 + (exp_5 * 5) + (exp_25 * 25) + (exp_100 * 100) + (exp_250 * 250);
        ArrayList<CloudCoin> totalCoins = IFileSystem.bankCoins;
        totalCoins.addAll(IFileSystem.frackedCoins);

        ArrayList<CloudCoin> exportCoins = new ArrayList<>();
        ArrayList<CloudCoin> fivesToExport = new ArrayList<>();
        ArrayList<CloudCoin> qtrToExport = new ArrayList<>();
        ArrayList<CloudCoin> hundredsToExport = new ArrayList<>();
        ArrayList<CloudCoin> twoFiftiesToExport = new ArrayList<>();

        for (int i = 0, totalCoinsSize = totalCoins.size(); i < totalCoinsSize; i++) {
            CloudCoin coin = totalCoins.get(i);
            int denomination = CoinUtils.getDenomination(coin);
            if (denomination == 1) {
                if (exp_1-- > 0) exportCoins.add(coin);
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
        exportCoins.addAll(fivesToExport);
        exportCoins.addAll(qtrToExport);
        exportCoins.addAll(hundredsToExport);
        exportCoins.addAll(twoFiftiesToExport);

        String filename;

        // Export Coins as Stack
        if ("1".equals(fileType)) {
            if ("1".equals(stackType)) { // Single Stack, or individual Stacks
                filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag);
                filename = FileUtils.ensureFilenameUnique(filename, ".stack");
                fileSystem.WriteCoinsToStack(exportCoins, filename);
            } else {
                for (CloudCoin coin : exportCoins) {
                    filename = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag;
                    filename = FileUtils.ensureFilenameUnique(filename, ".stack");
                    fileSystem.WriteCoinToStack(coin, filename);

                    updateLog("CloudCoin exported as Stack to " + filename);
                }
            }
        }

        // Export Coins as jpg Images
        else if ("2".equals(fileType)) {
            for (CloudCoin coin : exportCoins) {
                filename = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag;
                filename = FileUtils.ensureFilenameUnique(filename, ".jpg");
                boolean fileGenerated = fileSystem.WriteCoinToJpeg(coin, fileSystem.GetCoinTemplate(coin), filename);
                if (fileGenerated)
                    updateLog("CloudCoin exported as Jpg to " + filename);
            }
        }

        // Export Coins as CSV
        else if ("3".equals(fileType)) {
            filename = fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag;
            filename = FileUtils.ensureFilenameUnique(filename, ".csv");
            boolean fileGenerated = fileSystem.WriteCoinToCsv(exportCoins, filename);
            if (fileGenerated)
                updateLog("CloudCoin exported as Jpg to " + filename);
        }

        // Remove exported coins
        fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
        fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);
    }

    public void updateLog(String message) {
        System.out.println(message);
        logger.Info(message);
    }
}
