package com.cloudcore.exporter;

import com.cloudcore.exporter.core.*;
import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.FileUtils;
import com.cloudcore.exporter.utils.SimpleLogger;

import java.util.ArrayList;
import java.util.Scanner;

public class Exporter {


    /* Fields */

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


    /* Constructor */

    public Exporter(IFileSystem fileUtils) {
        this.fileSystem = fileUtils;
    }


    /* Methods */

    /**
     * Loads all CloudCoins from the Bank and Fracked folders, and saves the total number of files and denominations.
     */
    public void CalculateTotals() {
        // Count all Bank coins
        ArrayList<CloudCoin> bankCoins = fileSystem.loadFolderCoins(fileSystem.BankFolder);
        for (CloudCoin coin : bankCoins) {
            int denomination = CoinUtils.getDenomination(coin);

            if (denomination == 1) onesCount++;
            else if (denomination == 5) fivesCount++;
            else if (denomination == 25) qtrCount++;
            else if (denomination == 100) hundredsCount++;
            else if (denomination == 250) twoFiftiesCount++;
        }

        // Count all Fracked coins
        ArrayList<CloudCoin> frackedCoins = fileSystem.loadFolderCoins(fileSystem.FrackedFolder);
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

    /**
     * Asks the user for instructions on how to export CloudCoins to new files.
     */
    public void ExportCoins() {
        CalculateTotals();
        Scanner reader = new Scanner(System.in);
        int stackType = 1;

        // Ask for export type.
        System.out.println("Do you want to export your CloudCoin to (1) stacks (JSON), (2) JPG files, or (3) CSV files?");
        int fileType = reader.hasNextInt() ? reader.nextInt() : -1;
        updateLogNoPrint("User input: Export type: " + fileType);
        if (fileType < 0 || 3 < fileType) {
            updateLog("Invalid option. No CloudCoins were exported. Exiting...");
            return;
        }

        // Ask for export subtype for stacks.
        if (1 == fileType) {
            System.out.println("Export All Coins to Single Stack (1) or One Stack per coin (2)?");
            stackType = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: Stack type: " + stackType);
            if (!(stackType == 1 || stackType == 2)) {
                updateLog("Invalid option. No CloudCoins were exported. Exiting...");
                return;
            }
        }

        // Ask for amounts to export.
        int exp_1 = 0, exp_5 = 0, exp_25 = 0, exp_100 = 0, exp_250 = 0;
        boolean fail;
        if (onesTotalCount > 0) {
            System.out.println("How many 1s do you want to export? Available: " + onesTotalCount);
            exp_1 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 1s export: " + exp_1);
            if (exp_1 < 0 || exp_1 > onesTotalCount) {
                updateLog("Cannot export " + exp_1 + " CloudCoins from a pool of " + onesTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (fivesTotalCount > 0) {
            System.out.println("How many 5s do you want to export? Available: " + fivesTotalCount);
            exp_5 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 5s export: " + exp_5);
            if (exp_5 < 0 || exp_5 > onesTotalCount) {
                updateLog("Cannot export " + exp_5 + " CloudCoins from a pool of " + fivesTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if ((qtrTotalCount > 0)) {
            System.out.println("How many 25s do you want to export? Available: " + qtrTotalCount);
            exp_25 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 25s export: " + exp_25);
            if (exp_25 < 0 || exp_25 > qtrTotalCount) {
                updateLog("Cannot export " + exp_25 + " CloudCoins from a pool of " + qtrTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (hundredsTotalCount > 0) {
            System.out.println("How many 100s do you want to export? Available: " + hundredsTotalCount);
            exp_100 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 100s export: " + exp_100);
            if (exp_100 < 0 || exp_100 > hundredsTotalCount) {
                updateLog("Cannot export " + exp_100 + " CloudCoins from a pool of " + hundredsTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (twoFiftiesTotalCount > 0) {
            System.out.println("How many 250s do you want to export? Available: " + twoFiftiesTotalCount);
            exp_250 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 250s export: " + exp_250);
            if (exp_250 < 0 || exp_250 > twoFiftiesTotalCount) {
                updateLog("Cannot export " + exp_250 + " CloudCoins from a pool of " + twoFiftiesTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }

        if (exp_1 == 0 && exp_5 == 0 && exp_25 == 0 && exp_100 == 0 && exp_250 == 0) {
            updateLog("Exporting 0 CloudCoins. Done. Exiting...");
            return;
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
                    tag = '.' + tag;//.replace(' ', '_');
                break;
            }
        }
        updateLogNoPrint("User input: Tag: " + tag);

        // Get the CloudCoins that will be used for the export.
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

        if (onesToExport.size() < exp_1 || fivesToExport.size() < exp_5 || qtrToExport.size() < exp_25
                || hundredsToExport.size() < exp_100 || twoFiftiesToExport.size() < exp_250) {
            updateLog("Not enough CloudCoins for export. No CloudCoins were exported. Exiting...");
            return;
        }

        ArrayList<CloudCoin> exportCoins = new ArrayList<>();
        exportCoins.addAll(onesToExport);
        exportCoins.addAll(fivesToExport);
        exportCoins.addAll(qtrToExport);
        exportCoins.addAll(hundredsToExport);
        exportCoins.addAll(twoFiftiesToExport);

        String filename;

        // Export Coins as one Stack or individual Stacks
        if (1 == fileType) {
            if (1 == stackType) {
                filename = (fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag);
                filename = FileUtils.ensureFilenameUnique(filename, ".stack");
                fileSystem.WriteCoinsToSingleStack(exportCoins, filename);
                updateLog("CloudCoins exported as single Stack to " + filename + ".stack");
            } else {
                for (CloudCoin coin : exportCoins) {
                    filename = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag;
                    filename = FileUtils.ensureFilenameUnique(filename, ".stack");
                    fileSystem.WriteCoinToIndividualStacks(coin, filename);

                    updateLog("CloudCoin exported as Stack to " + filename + ".stack");
                }
            }
        }

        // Export Coins as jpg Images
        else if (2 == fileType) {
            for (CloudCoin coin : exportCoins) {
                filename = fileSystem.ExportFolder + CoinUtils.getFilename(coin) + tag;
                filename = FileUtils.ensureFilenameUnique(filename, ".jpg");
                boolean fileGenerated = fileSystem.writeCoinToJpeg(coin, fileSystem.GetCoinTemplate(coin), filename);
                if (fileGenerated)
                    updateLog("CloudCoin exported as JPG to " + filename + ".jpg");
            }
        }

        // Export Coins as CSV
        else if (3 == fileType) {
            filename = fileSystem.ExportFolder + totalSaved + ".CloudCoins" + tag;
            filename = FileUtils.ensureFilenameUnique(filename, ".csv");
            boolean fileGenerated = fileSystem.writeCoinToCsv(exportCoins, filename);
            if (fileGenerated)
                updateLog("CloudCoin exported as CSV to " + filename + ".csv");
        }

        // Remove exported coins
        fileSystem.RemoveCoins(exportCoins, fileSystem.BankFolder);
        fileSystem.RemoveCoins(exportCoins, fileSystem.FrackedFolder);
    }

    /**
     * Sends a message to the SimpleLogger, and prints the message to console.
     *
     * @param message a log message.
     */
    private void updateLog(String message) {
        System.out.println(message);
        logger.appendLog(message);
    }

    /**
     * Sends a message to the SimpleLogger.
     *
     * @param message a log message.
     */
    private void updateLogNoPrint(String message) {
        logger.appendLog(message);
    }
}
