package com.cloudcore.exporter.core;

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
import java.util.Arrays;

public class FileSystem extends IFileSystem {

    public FileSystem(String RootPath) {
        this.RootPath = RootPath;
        ImportFolder = RootPath + File.separator + Config.TAG_IMPORT + File.separator;
        ExportFolder = RootPath + File.separator + Config.TAG_EXPORT + File.separator;
        TemplateFolder = RootPath + File.separator + Config.TAG_TEMPLATES + File.separator;
        FrackedFolder = RootPath + File.separator + Config.TAG_FRACKED + File.separator;
        BankFolder = RootPath + File.separator + Config.TAG_BANK + File.separator;
        LogsFolder = RootPath + File.separator + Config.TAG_LOGS + File.separator;
        QRFolder = ImportFolder + Config.TAG_QR;
        BarCodeFolder = ImportFolder + Config.TAG_BARCODE;
        CSVFolder = ImportFolder + Config.TAG_CSV;

    }

    public boolean CreateDirectories() {
        // Create Subdirectories as per the RootFolder Location
        // Failure will return false

        try {
            Files.createDirectories(Paths.get(RootPath));

            Files.createDirectories(Paths.get(ImportFolder));
            Files.createDirectories(Paths.get(ExportFolder));
            Files.createDirectories(Paths.get(BankFolder));
            Files.createDirectories(Paths.get(FrackedFolder));
            Files.createDirectories(Paths.get(TemplateFolder));
            Files.createDirectories(Paths.get(QRFolder));
            Files.createDirectories(Paths.get(BarCodeFolder));
            Files.createDirectories(Paths.get(CSVFolder));

            Files.createDirectories(Paths.get(LogsFolder));
        } catch (Exception e) {
            System.out.println("FS#CD: " + e.getLocalizedMessage());
            e.printStackTrace();
            return false;
        }


        return true;
    }

    public void LoadFileSystem() {
        importCoins = LoadFolderCoins(ImportFolder);
        ArrayList<CloudCoin> csvCoins = LoadCoinsByFormat(ImportFolder + File.separator + "CSV", Formats.CSV);
        ArrayList<CloudCoin> qrCoins = LoadCoinsByFormat(ImportFolder + File.separator + "QrCodes", Formats.QRCode);
        ArrayList<CloudCoin> BarCodeCoins = LoadCoinsByFormat(ImportFolder + File.separator + "Barcodes", Formats.BarCode);

        // Add Additional File formats if present
        //importCoins = importCoins.Concat(csvCoins);
        importCoins.addAll(BarCodeCoins);
        importCoins.addAll(qrCoins);

        //exportCoins = LoadFolderCoins(ExportFolder);
        bankCoins = LoadFolderCoins(BankFolder);
        frackedCoins = LoadFolderCoins(FrackedFolder);
        //importedCoins = LoadFolderCoins(ImportedFolder);
        //trashCoins = LoadFolderCoins(TrashFolder);
        //LoadFolderCoins(TemplateFolder);
        //counterfeitCoins = LoadFolderCoins(CounterfeitFolder);

    }

    @Override
    public boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String OutputFile, String tag) {
        OutputFile = OutputFile.replace("\\\\", "\\");

        // BUILD THE CLOUDCOIN STRING //
        StringBuilder cloudCoinBuilder = new StringBuilder();

        // APP0 Marker
        cloudCoinBuilder.append("01C34A46494600010101006000601D05");
        // ANs (400 bytes)
        for (int i = 0; (i < 25); i++)
            cloudCoinBuilder.append(cloudCoin.an.get(i));
        // AOID
        cloudCoinBuilder.append("00000000000000000000000000000000"); // Set to unknown so program does not export user data
        // POWN
        cloudCoinBuilder.append(bytesToHexString(cloudCoin.pown.getBytes(StandardCharsets.UTF_8)));
        // HC (Has Comments)
        cloudCoinBuilder.append("00"); // HC: Has comments. 00 = No
        // ED (Months since August 2016)
        cloudCoin.CalcExpirationDate();
        cloudCoinBuilder.append(bytesToHexString(cloudCoin.edHex.getBytes(StandardCharsets.UTF_8))); // 01; // Expiration date Sep 2016 (one month after zero month)
        // NN
        cloudCoinBuilder.append("01");
        // SN
        String fullHexSN = Utils.padString(Integer.toHexString(cloudCoin.getSn()).toUpperCase(), 6, '0');
        cloudCoinBuilder.append(fullHexSN);


        System.out.println("edHex: " + bytesToHexString(cloudCoin.edHex.getBytes(StandardCharsets.UTF_8)));
        System.out.println("ccArrayString: " + cloudCoinBuilder.toString());
        // BYTES THAT WILL GO FROM 04 to 454 (Inclusive)//
        byte[] ccArray = DatatypeConverter.parseHexBinary(cloudCoinBuilder.toString());

        System.out.println("ccArray: " + Arrays.toString(ccArray));
        System.out.println("ccArray length: " + Arrays.toString(ccArray).length());

        // JPEG image data
        try {
            byte[] jpegBytes = Files.readAllBytes(Paths.get(TemplateFile));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jpegBytes);
            BufferedImage image = ImageIO.read(inputStream);

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawString(cloudCoin.getSn() + " of 16,777,216 on Network: 1", 30, 50);

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
            System.arraycopy(imageBytes, 4, outputBytes, 4 + ccArray.length, 4);

            Files.write(Paths.get(OutputFile), imageBytes);
            //System.out.println("Writing to " + fileName);
            //CoreLogger.Log("Writing to " + fileName);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean WriteCoinToQRCode(CloudCoin cloudCoin, String OutputFile) {
        /* TODO QRCODE
            int width = 250; // width of the Qr Code
            int height = 250; // height of the Qr Code
            int margin = 0;
            var qrCodeWriter = new ZXing.BarcodeWriterPixelData()
            {
                Format = ZXing.BarcodeFormat.QR_CODE,
                        Options = new QrCodeEncodingOptions
                {
                    Height = height,
                            Width = width,
                            Margin = margin
                }
            };
            String coinJson = JsonConvert.SerializeObject(cloudCoin);
            var pixelData = qrCodeWriter.Write(coinJson);
            // creating a bitmap from the raw pixel data; if only black and white colors are used it makes no difference
            // that the pixel data ist BGRA oriented and the bitmap is initialized with RGB
            using (var bitmap = new System.Drawing.Bitmap(pixelData.Width, pixelData.Height, System.Drawing.Imaging.PixelFormatFormat32bppRgb))
            using (var ms = new MemoryStream())
            {
                var bitmapData = bitmap.LockBits(new System.Drawing.Rectangle(0, 0, pixelData.Width, pixelData.Height), System.Drawing.Imaging.ImageLockMode.WriteOnly, System.Drawing.Imaging.PixelFormatFormat32bppRgb);
                try
                {
                    // we assume that the row stride of the bitmap is aligned to 4 byte multiplied by the width of the image
                    System.Runtime.InteropServices.Marshal.Copy(pixelData.Pixels, 0, bitmapData.Scan0, pixelData.Pixels.length);
                }
                finally
                {
                    bitmap.UnlockBits(bitmapData);
                }
                // save to stream as PNG
                bitmap.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                bitmap.Save(OutputFile);
            }*/

            return true;
    }

    @Override
    public boolean WriteCoinToBARCode(CloudCoin cloudCoin, String OutputFile) {
            /* TODO BARCODE
            var writer = new BarcodeWriter()
            {
                Format = BarcodeFormat.PDF_417,
                        Options = new EncodingOptions { Width = 200, Height = 50 } //optional
            };
            cloudCoin.pan = null;
            var coinJson = JsonConvert.SerializeObject(cloudCoin);
            var imgBitmap = writer.Write(coinJson);
            using (var stream = new MemoryStream())
            {
                imgBitmap.Save(stream, ImageFormat.Png);
                stream.toArray();
                imgBitmap.Save(OutputFile);
            }*/
            return true;
    }
}

