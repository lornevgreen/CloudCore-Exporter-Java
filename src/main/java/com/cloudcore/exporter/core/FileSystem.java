package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.FileUtils;
import com.cloudcore.exporter.utils.Utils;
import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileSystem {


    /* Fields */

    public static String RootPath = "C:" + File.separator + "CloudCoins-Exporter" + File.separator;

    public static String ExportFolder = RootPath + Config.TAG_EXPORT + File.separator;

    public static String BankFolder = RootPath + Config.TAG_BANK + File.separator;
    public static String FrackedFolder = RootPath + Config.TAG_FRACKED + File.separator;

    public static String LogsFolder = RootPath + Config.TAG_LOGS + File.separator;
    public static String TemplateFolder = RootPath + Config.TAG_TEMPLATES + File.separator;

    public static ArrayList<CloudCoin> bankCoins;
    public static ArrayList<CloudCoin> frackedCoins;


    /* Methods */

    /**
     * Creates directories in the location defined by RootPath.
     *
     * @return true if all folders were created or already exist, otherwise false.
     */
    public static boolean createDirectories() {
        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(ExportFolder));
            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(TemplateFolder));
            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Loads to memory all of the CloudCoins in the Bank and Fracked folders.
     */
    public static void loadFileSystem() {
        bankCoins = loadFolderCoins(BankFolder);
        frackedCoins = loadFolderCoins(FrackedFolder);
    }



    /**
     * Load all CloudCoins in a specific folder.
     *
     * @param folder the folder to search for CloudCoin files.
     * @return an ArrayList of all CloudCoins in the specified folder.
     */
    public static ArrayList<CloudCoin> loadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] filenames = FileUtils.selectFileNamesInFolder(folder);
        for (String filename : filenames) {
            int index = filename.lastIndexOf('.');
            if (index == -1) continue;

            String extension = filename.substring(index + 1);

            switch (extension) {
                case "stack":
                    ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromStack(folder, filename);
                    folderCoins.addAll(coins);
                    break;
                case "jpg":
                case "jpeg":
                    CloudCoin coin = importJPG(folder, filename);
                    folderCoins.add(coin);
                    break;
                case "csv":
                    ArrayList<String> lines;
                    String fullFilePath = folder + filename;
                    try {
                        ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                        lines = new ArrayList<>(Files.readAllLines(Paths.get(fullFilePath)));
                        for (String line : lines)
                            csvCoins.add(CoinUtils.cloudCoinFromCsv(line, folder, filename));
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
     * @param folder the folder containing the jpg file.
     * @param filename the absolute filepath of the jpg file.
     */
    private static CloudCoin importJPG(String folder, String filename) {
        String fullFilePath = folder + filename;
        try {
            // read until Read method returns 0 (end of the stream has been reached)
            byte[] headerBytes = new byte[455];
            int count, sum = 0;

            FileInputStream inputStream = new FileInputStream(new File(fullFilePath));
            while ((count = inputStream.read(headerBytes, sum, 455 - sum)) > 0) {
                sum += count; // sum is a buffer offset for next reading
            }
            inputStream.close();

            String header = bytesToHexString(headerBytes);
            return CoinUtils.cloudCoinFromJpgHeader(header, folder, filename);
        } catch (IOException e) {
            System.out.println("IO Exception:" + fullFilePath + e);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes CloudCoins files from a specific folder.
     *
     * @param cloudCoins the ArrayList of CloudCoins to delete.
     * @param folder     the folder to delete from.
     */
    public static void removeCoins(ArrayList<CloudCoin> cloudCoins, String folder) {
        for (CloudCoin coin : cloudCoins) {
            try {
                Files.deleteIfExists(Paths.get(folder + coin.folder + coin.currentFilename));
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
    public static void writeCoinsToSingleStack(ArrayList<CloudCoin> coins, String filePath) {
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
    public static void writeCoinToIndividualStacks(CloudCoin coin, String filePath) {
        Stack stack = new Stack(coin);
        try {
            Files.write(Paths.get(filePath + ".stack"), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the full file path for a JPG image template.
     *
     * @param cloudCoin the CloudCoin that needs a JPG image template.
     * @return the full file path for a JPG image template.
     */
    public static String getJpgTemplate(CloudCoin cloudCoin) {
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
    public static String bytesToHexString(byte[] data) {
        final String HexChart = "0123456789ABCDEF";
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(HexChart.charAt((b & 0xF0) >> 4)).append(HexChart.charAt((b & 0x0F)));
        return hex.toString();
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
    public static boolean writeCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String filePath) {
        StringBuilder jpgHeader = new StringBuilder();

        // APP0 Marker
        jpgHeader.append("01C34A46494600010101006000601D05");
        // ANs (400 bytes)
        for (int i = 0; (i < 25); i++)
            jpgHeader.append(cloudCoin.getAn().get(i));
        // AOID
        jpgHeader.append("00000000000000000000000000000000"); // Set to unknown so program does not export user data
        // POWN
        jpgHeader.append(CoinUtils.pownStringToHex(cloudCoin));
        // HC (Has Comments) 0 = No
        jpgHeader.append("00");
        // ED (Expiration Date; months since August 2016)
        jpgHeader.append(CoinUtils.expirationDateStringToHex());
        // NN
        jpgHeader.append("01");
        // SN
        String fullHexSN = Utils.padString(Integer.toHexString(cloudCoin.getSn()).toUpperCase(), 6, '0');
        jpgHeader.append(fullHexSN);

        // BYTES THAT WILL GO FROM 04 to 454 (Inclusive)//
        byte[] ccArray = DatatypeConverter.parseHexBinary(jpgHeader.toString());

        try {
            // JPEG image data
            byte[] jpegBytes = Files.readAllBytes(Paths.get(TemplateFile));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jpegBytes);
            BufferedImage image = ImageIO.read(inputStream);

            // Set high quality rendering to draw text on image
            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawString(cloudCoin.getSn() + " of 16,777,216 on Network: 1", 30, 50);

            // Save the image bytes
            graphics.drawImage(image, null, 0, 0);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
            byte[] imageBytes = outputStream.toByteArray();
            outputStream.close();

            // outputBytes is imageBytes plus ccArray, with ccArray starting at index 4 in imageBytes
            byte[] outputBytes = new byte[ccArray.length + imageBytes.length];
            System.arraycopy(imageBytes, 0, outputBytes, 0, 4);
            System.arraycopy(ccArray, 0, outputBytes, 4, ccArray.length);
            System.arraycopy(imageBytes, 4, outputBytes, 4 + ccArray.length, imageBytes.length - 4);

            Files.write(Paths.get(filePath + ".jpg"), outputBytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Writes an array of CloudCoin objects to a CSV file.
     *
     * @param cloudCoins the ArrayList of CloudCoin objects.
     * @param filePath   the absolute filepath of the new CloudCoin-embedded JPG, without the extension.
     * @return true if the file was saved to the location provided, otherwise false.
     */
    public static boolean writeCoinsToCsv(ArrayList<CloudCoin> cloudCoins, String filePath) {
        StringBuilder csv = new StringBuilder();

        // Header
        csv.append("sn,denomination,nn,");
        // ANs Header
        for (int i = 0; i < Config.nodeCount; i++)
            csv.append("an").append((i + 1)).append(",");
        // new line
        csv.append(System.lineSeparator());

        // CloudCoins
        for (CloudCoin coin : cloudCoins) {
            // SN
            csv.append(coin.getSn()).append(',');
            // Denomination
            csv.append(CoinUtils.getDenomination(coin)).append(',');
            // Network Number
            csv.append(coin.getNn()).append(',');
            // ANs
            for (int i = 0; i < Config.nodeCount; i++)
                csv.append(coin.getAn().get(i)).append(",");
            // new line
            csv.append(System.lineSeparator());
        }

        // Write the file
        try {
            Files.write(Paths.get(filePath + ".csv"), csv.toString().getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

