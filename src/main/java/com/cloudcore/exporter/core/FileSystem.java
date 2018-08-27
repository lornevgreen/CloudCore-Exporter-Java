package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.CoinUtils;
import com.cloudcore.exporter.utils.Utils;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FileSystem extends IFileSystem {


    /* Constructor */

    public FileSystem(String RootPath) {
        this.RootPath = RootPath;
        ExportFolder = RootPath + File.separator + Config.TAG_EXPORT + File.separator;
        TemplateFolder = RootPath + File.separator + Config.TAG_TEMPLATES + File.separator;
        FrackedFolder = RootPath + File.separator + Config.TAG_FRACKED + File.separator;
        BankFolder = RootPath + File.separator + Config.TAG_BANK + File.separator;
        LogsFolder = RootPath + File.separator + Config.TAG_LOGS + File.separator;
    }


    /* Methods */

    /**
     * Creates directories in the location defined by RootPath.
     *
     * @return true if all folders were created or already exist, otherwise false.
     */
    public boolean createDirectories() {
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
    public void loadFileSystem() {
        bankCoins = loadFolderCoins(BankFolder);
        frackedCoins = loadFolderCoins(FrackedFolder);
    }

    @Override
    public boolean writeCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String filePath) {
        StringBuilder jpgHeader = new StringBuilder();

        // APP0 Marker
        jpgHeader.append("01C34A46494600010101006000601D05");
        // ANs (400 bytes)
        for (int i = 0; (i < 25); i++)
            jpgHeader.append(cloudCoin.an.get(i));
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

    @Override
    public boolean writeCoinsToCsv(ArrayList<CloudCoin> cloudCoins, String filePath) {
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
            csv.append(coin.nn).append(',');
            // ANs
            for (int i = 0; i < Config.nodeCount; i++)
                csv.append(coin.an.get(i)).append(",");
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

