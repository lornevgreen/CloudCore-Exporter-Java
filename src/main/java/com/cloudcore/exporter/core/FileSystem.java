package com.cloudcore.exporter.core;

import com.cloudcore.exporter.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
        boolean fileSavedSuccessfully = true;

        // * TODO JPEG
        // BUILD THE CLOUDCOIN STRING //
        StringBuilder cloudCoinBuilder = new StringBuilder();
        cloudCoinBuilder.append("01C34A46494600010101006000601D05"); //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
            cloudCoinBuilder.append(cloudCoin.an.get(i));

        //cloudCoinStr.append("204f42455920474f4420262044454645415420545952414e545320"); // Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr.append("20466f756e6465727320372d352d3137"); // Founders 7-5-17
        cloudCoinBuilder.append("4c6976652046726565204f7220446965"); // Live Free or Die
        cloudCoinBuilder.append("00000000000000000000000000"); // Set to unknown so program does not export user data
        cloudCoinBuilder.append("00"); // HC: Has comments. 00 = No
        cloudCoin.CalcExpirationDate();
        cloudCoinBuilder.append(cloudCoin.edHex); // 01; // Expiration date Sep 2016 (one month after zero month)
        cloudCoinBuilder.append("01"); // cc.nn; // network number
        String fullHexSN = Utils.padString(Integer.toHexString(cloudCoin.getSn()).toUpperCase(), 6, '0');
        cloudCoinBuilder.append(fullHexSN);
        // BYTES THAT WILL GO FROM 04 to 454 (Inclusive)//
        byte[] ccArray = this.hexStringToByteArray(cloudCoinBuilder.toString());

        try {
            byte[] jpegBytes = Files.readAllBytes(Paths.get(TemplateFile));
            ByteArrayInputStream inputStream = new ByteArrayInputStream(jpegBytes);
            BufferedImage image = ImageIO.read(inputStream);

            Graphics2D graphics = (Graphics2D) image.getGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.drawString(cloudCoin.getSn() + " of 16,777,216 on Network: 1", 30, 25);

            graphics.drawImage(image, null, 0, 0);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", outputStream);
            outputStream.flush();
            byte[] imageBytes = outputStream.toByteArray();
            outputStream.close();

            // outputBytes is imageBytes and ccArray, with ccArray starting at index 4 in imageBytes
            byte[] outputBytes = new byte[ccArray.length + imageBytes.length];
            System.arraycopy(imageBytes, 0, outputBytes, 0, 4);
            System.arraycopy(ccArray, 0, outputBytes, 4, ccArray.length);
            System.arraycopy(imageBytes, 4, outputBytes, 4 + ccArray.length, 4);

            //ArrayList<Byte> b1 = new ArrayList<Byte>(Arrays.asList(imageBytes));
            //ArrayList<Byte> b2 = new ArrayList<Byte>(ccArray);
            //b1.addAll(4, b2);


            //byte[] output = Utils.combine(imageBytes, new byte[], )
            Files.write(Paths.get(OutputFile), imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // WRITE THE SERIAL NUMBER ON THE JPEG //
/*
        Bitmap bitmapimage;

        using (var ms = new MemoryStream(jpegBytes))
        {
            bitmapimage = new Bitmap(ms);
        }

        Graphics graphics = Graphics.FromImage(bitmapimage);
        graphics.SmoothingMode = SmoothingMode.AntiAlias;
        graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        PointF drawPointAddress = new PointF(30.0F, 25.0F);
        graphics.DrawString(String.format("{0:N0}", cloudCoin.getSn()) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        ImageConverter converter = new ImageConverter();
        byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));

        ArrayList<Byte> b1 = new ArrayList<Byte>(snBytes);
        ArrayList<Byte> b2 = new ArrayList<Byte>(ccArray);
        b1.addAll(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.toString();
        }

        //String fileName = targetPath;

        String fileName = ExportFolder + cloudCoin.FileName + ".jpg";
        File.WriteAllBytes(OutputFile, b1.toArray());
        //System.out.println("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);*/

        return fileSavedSuccessfully;
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

