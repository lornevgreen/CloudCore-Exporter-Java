package com.cloudcore.exporter;

import com.cloudcore.exporter.core.*;
import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.FileUtils;
import com.cloudcore.exporter.utils.SimpleLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Exporter {


    /* Fields */
    
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


    /* Methods */

    /**
     * Loads all CloudCoins from the Bank and Fracked folders, and saves the total number of files and denominations.
     */
    public static void CalculateTotals(String accountFolder) {
        // Count all Bank coins
        ArrayList<CloudCoin> bankCoins = FileSystem.loadFolderCoinsExport(accountFolder + FileSystem.BankPath);
        for (CloudCoin coin : bankCoins) {
            int denomination = CoinUtils.getDenomination(coin);

            if (denomination == 1) onesCount++;
            else if (denomination == 5) fivesCount++;
            else if (denomination == 25) qtrCount++;
            else if (denomination == 100) hundredsCount++;
            else if (denomination == 250) twoFiftiesCount++;
        }

        // Count all Fracked coins
        ArrayList<CloudCoin> frackedCoins = FileSystem.loadFolderCoinsExport(accountFolder + FileSystem.FrackedPath);
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

    public static int[] calculateNotesForTotal(String accountFolder, int amount) {
        int[] coinTotals = FileSystem.getTotalCoinsBank(FileSystem.AccountFolder + accountFolder);
        int[] notesToExport = new int[coinTotals.length];

        if (amount >= 250 && coinTotals[4] > 0) {
            notesToExport[4] = ((amount / 250) < (coinTotals[4])) ? (amount / 250) : (coinTotals[4]);
            amount -= (notesToExport[4] * 250);
        }
        if (amount >= 100 && coinTotals[3] > 0) {
            notesToExport[3] = ((amount / 100) < (coinTotals[3])) ? (amount / 100) : (coinTotals[3]);
            amount -= (notesToExport[3] * 100);
        }
        if (amount >= 25 && coinTotals[2] > 0) {
            notesToExport[2] = ((amount / 25) < (coinTotals[2])) ? (amount / 25) : (coinTotals[2]);
            amount -= (notesToExport[2] * 25);
        }
        if (amount >= 5 && coinTotals[1] > 0) {
            notesToExport[1] = ((amount / 5) < (coinTotals[1])) ? (amount / 5) : (coinTotals[1]);
            amount -= (notesToExport[1] * 5);
        }
        if (amount >= 1 && coinTotals[0] > 0) {
            notesToExport[0] = (amount < (coinTotals[0])) ? amount : (coinTotals[0]);
            amount -= (notesToExport[0]);
        }

        if (amount != 0)
            return new int[0];
        else
            return notesToExport;
    }

    private static void askUserForExportParameters() {
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
        if (onesTotalCount > 0) {
            System.out.println("How many 1s do you want to export? Available: " + onesTotalCount);
            exp_1 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 1s export: " + exp_1);
            if (exp_1 < 0 || exp_1 > onesTotalCount) {
                updateLog("Cannot export " + exp_1 + " 1-CloudCoins from a pool of " + onesTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (fivesTotalCount > 0) {
            System.out.println("How many 5s do you want to export? Available: " + fivesTotalCount);
            exp_5 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 5s export: " + exp_5);
            if (exp_5 < 0 || exp_5 > fivesTotalCount) {
                updateLog("Cannot export " + exp_5 + " 5-CloudCoins from a pool of " + fivesTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if ((qtrTotalCount > 0)) {
            System.out.println("How many 25s do you want to export? Available: " + qtrTotalCount);
            exp_25 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 25s export: " + exp_25);
            if (exp_25 < 0 || exp_25 > qtrTotalCount) {
                updateLog("Cannot export " + exp_25 + " 25-CloudCoins from a pool of " + qtrTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (hundredsTotalCount > 0) {
            System.out.println("How many 100s do you want to export? Available: " + hundredsTotalCount);
            exp_100 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 100s export: " + exp_100);
            if (exp_100 < 0 || exp_100 > hundredsTotalCount) {
                updateLog("Cannot export " + exp_100 + " 100-CloudCoins from a pool of " + hundredsTotalCount + " CloudCoins. Exiting...");
                return;
            }
        }
        if (twoFiftiesTotalCount > 0) {
            System.out.println("How many 250s do you want to export? Available: " + twoFiftiesTotalCount);
            exp_250 = reader.hasNextInt() ? reader.nextInt() : -1;
            updateLogNoPrint("User input: 250s export: " + exp_250);
            if (exp_250 < 0 || exp_250 > twoFiftiesTotalCount) {
                updateLog("Cannot export " + exp_250 + " 250-CloudCoins from a pool of " + twoFiftiesTotalCount + " CloudCoins. Exiting...");
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
    }

    /**
     * Asks the user for instructions on how to export CloudCoins to new files.
     */
    public static boolean ExportCoins(int[] exp, int fileType, String accountId, String tag) {
        System.out.println("exporting...");
        String accountFolder = FileSystem.AccountFolder + accountId + File.separator;

        if (exp.length == 0 || fileType < 0 || fileType > 3) {
            updateLog("export commands are wrong " + exp.length + ", " + fileType);
            return false;
        }

        if (tag == null)
            tag = "";

        // Get the CloudCoins that will be used for the export.
        int totalSaved = exp[0] + (exp[1] * 5) + (exp[2] * 25) + (exp[3] * 100) + (exp[4] * 250);
        ArrayList<CloudCoin> totalCoins = FileSystem.loadFolderCoinsExport(accountFolder + FileSystem.BankPath);
        totalCoins.addAll(FileSystem.loadFolderCoinsExport(accountFolder + FileSystem.FrackedPath));

        ArrayList<CloudCoin> onesToExport = new ArrayList<>();
        ArrayList<CloudCoin> fivesToExport = new ArrayList<>();
        ArrayList<CloudCoin> qtrToExport = new ArrayList<>();
        ArrayList<CloudCoin> hundredsToExport = new ArrayList<>();
        ArrayList<CloudCoin> twoFiftiesToExport = new ArrayList<>();

        System.out.println(totalCoins.size() + " coins");

        for (int i = 0, j = totalCoins.size(); i < j; i++) {
            CloudCoin coin = totalCoins.get(i);
            int denomination = CoinUtils.getDenomination(coin);
            if (denomination == 1) {
                if (exp[0]-- > 0) onesToExport.add(coin);
                else exp[0] = 0;
            } else if (denomination == 5) {
                if (exp[1]-- > 0) {
                    fivesToExport.add(coin);
                } else exp[1] = 0;
            } else if (denomination == 25) {
                if (exp[2]-- > 0) qtrToExport.add(coin);
                else exp[2] = 0;
            } else if (denomination == 100) {
                if (exp[3]-- > 0) hundredsToExport.add(coin);
                else exp[3] = 0;
            } else if (denomination == 250) {
                if (exp[4]-- > 0) twoFiftiesToExport.add(coin);
                else exp[4] = 0;
            }
        }

        if (onesToExport.size() < exp[0] || fivesToExport.size() < exp[1] || qtrToExport.size() < exp[2]
                || hundredsToExport.size() < exp[3] || twoFiftiesToExport.size() < exp[4]) {
            updateLog("Not enough CloudCoins for export. No CloudCoins were exported. Exiting...");
            return false;
        }

        ArrayList<CloudCoin> exportCoins = new ArrayList<>();
        exportCoins.addAll(onesToExport);
        exportCoins.addAll(fivesToExport);
        exportCoins.addAll(qtrToExport);
        exportCoins.addAll(hundredsToExport);
        exportCoins.addAll(twoFiftiesToExport);

        String filename;
        String exportFolder = accountFolder + FileSystem.ExportPath;

        // Export Coins as multiple notes
        if (0 == fileType) {
            for (CloudCoin coin : exportCoins) {
                FileSystem.saveCoin(coin, exportFolder);
                updateLog("CloudCoins exported as Stacks to " + coin.currentFilename);
            }
        }

        // Export Coins as one Stack
        if (1 == fileType) {
            filename = totalSaved + ".CloudCoins" + tag;
            filename = FileUtils.ensureFilenameUnique(filename, ".stack", exportFolder);
            FileSystem.saveCoinsSingleStack(exportCoins, exportFolder + filename);
            updateLog("CloudCoins exported as single Stack to " + filename);
        }

        // Export Coins as jpg Images
        else if (2 == fileType) {
            for (CloudCoin coin : exportCoins) {
                filename = CoinUtils.generateFilename(coin) + tag;
                filename = FileUtils.ensureFilenameUnique(filename, ".jpg", exportFolder);
                boolean fileGenerated = FileSystem.saveCoinJpg(coin, exportFolder + filename);
                if (fileGenerated)
                    updateLog("CloudCoin exported as JPG to " + filename);
            }
        }

        // Export Coins as CSV
        else if (3 == fileType) {
            filename = totalSaved + ".CloudCoins" + tag;
            filename = FileUtils.ensureFilenameUnique(filename, ".csv", exportFolder);
            boolean fileGenerated = FileSystem.saveCoinsCsv(exportCoins, exportFolder + filename);
            if (fileGenerated)
                updateLog("CloudCoin exported as CSV to " + filename);
        }

        //saveRemainingCoins(totalCoins, exportCoins);

        // Remove exported coins
        FileSystem.removeCoins(exportCoins, accountFolder + FileSystem.BankPath);
        FileSystem.removeCoins(exportCoins, accountFolder + FileSystem.FrackedPath);

        return true;
    }

    private static void saveRemainingCoins(ArrayList<CloudCoin> totalCoins, ArrayList<CloudCoin> exportCoins,
                                           String accountFolder) {
        totalCoins.removeAll(exportCoins);

        if (totalCoins.size() == 0)
            return;

        String filename = totalCoins.size() + ".CloudCoins";
        filename = FileUtils.ensureFilenameUnique(filename, ".stack", accountFolder + FileSystem.ExportPath);
        FileSystem.saveCoinsSingleStack(exportCoins, accountFolder + FileSystem.ExportPath + filename);
        updateLog("CloudCoins exported as single Stack to " + filename);
    }

    /**
     * Sends a message to the SimpleLogger, and prints the message to console.
     *
     * @param message a log message.
     */
    private static void updateLog(String message) {
        if (Config.DEBUG_MODE)
            System.out.println(message);
        SimpleLogger.writeLog(message, "");
    }

    /**
     * Sends a message to the SimpleLogger.
     *
     * @param message a log message.
     */
    private static void updateLogNoPrint(String message) {
        SimpleLogger.writeLog(message, "");
    }
}
