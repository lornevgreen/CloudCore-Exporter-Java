package com.cloudcore.exporter.core;

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

    public enum FileMoveOptions {Replace, Rename}

    public String RootPath;
    public String ImportFolder;
    public String ExportFolder;
    public String BankFolder;
    public String ImportedFolder;
    public String LostFolder;
    public String TrashFolder;
    public String SuspectFolderOld;
    public String DetectedFolder;
    public String FrackedFolder;
    public String TemplateFolder;
    public String PartialFolder;
    public String CounterfeitFolder;
    public String LanguageFolder;
    public String SuspectFolder;
    public String RequestsFolder;
    public String DangerousFolder;
    public String LogsFolder;
    public String QRFolder;
    public String BarCodeFolder;
    public String CSVFolder;

    //public abstract IFileSystem(String path);

    public static ArrayList<CloudCoin> importCoins;
    public static ArrayList<CloudCoin> frackedCoins;
    public static ArrayList<CloudCoin> bankCoins;

    public ArrayList<CloudCoin> LoadCoinsByFormat(String folder, Formats format) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();
        CloudCoin coin;

        String allowedExtension = "";
        switch (format) {
            case CSV:
                allowedExtension = "csv";
                break;
            case BarCode:
            case QRCode:
                allowedExtension = "jpg";
                break;
        }

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (String fileName : fileNames) {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);

                if (allowedExtension.equalsIgnoreCase(extension)) {
                    switch (format) {
                        case CSV:
                            ArrayList<CloudCoin> csvCoins = ReadCSVCoins(fileName);
                            csvCoins.remove(null);
                            folderCoins.addAll(csvCoins);
                            break;
                        case BarCode:
                            //TODO BARCODE coin = ReadBARCode(fileName);
                            //folderCoins.add(coin);
                            break;
                        case QRCode:
                            //TODO QRCODE coin = ReadQRCode(fileName);
                            //folderCoins.add(coin);
                            break;
                    }
                }
            }
        }

        return folderCoins;
    }

    public ArrayList<CloudCoin> LoadFolderCoins(String folder) {
        ArrayList<CloudCoin> folderCoins = new ArrayList<>();

        String[] fileNames = FileUtils.selectFileNamesInFolder(folder);
        String extension;
        for (String fileName : fileNames) {
            int index = fileName.lastIndexOf('.');
            if (index > 0) {
                extension = fileName.substring(index + 1);
                fileName = folder + fileName;

                switch (extension) {
                    case "celeb":
                    case "celebrium":
                    case "stack":
                        ArrayList<CloudCoin> coins = FileUtils.loadCloudCoinsFromJSON(fileName);
                        if (coins != null)
                            folderCoins.addAll(coins);
                        break;
                    case "jpg":
                    case "jpeg":
                        CloudCoin coin = importJPEG(fileName);
                        folderCoins.add(coin);
                        break;
                    case "csv":
                        ArrayList<String> lines;
                        try {
                            ArrayList<CloudCoin> csvCoins = new ArrayList<>();
                            lines = new ArrayList<>(Files.readAllLines(Paths.get(fileName)));
                            for (String line : lines) {
                                csvCoins.add(CloudCoin.FromCSV(line));
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
/* TODO QRCODE BARCODE
    // Read a CloudCoin from QR Code
    private CloudCoin ReadQRCode(String fileName)
    {
        try
        {
            Bitmap bitmap = new Bitmap(fileName);
            BarcodeReader reader = new BarcodeReader { AutoRotate = true, TryInverted = true };
            //Result result = reader.Decode(bitmap);
            String decoded = result.toString().Trim();

            CloudCoin cloudCoin = JsonConvert.DeserializeObject<CloudCoin>(decoded);
            return cloudCoin;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private CloudCoin ReadBARCode(String fileName)//Read a CloudCoin from BAR Code . 
    {
        try
        {
            var barcodeReader = new BarcodeReader();
            Bitmap bitmap = new Bitmap(fileName);

            var barcodeResult = barcodeReader.Decode(bitmap);
            String decoded = barcodeResult.toString().Trim();

            CloudCoin cloudCoin = JsonConvert.DeserializeObject<CloudCoin>(decoded);
            return cloudCoin;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
*/
    private ArrayList<CloudCoin> ReadCSVCoins(String fileName)//Read a CloudCoin from CSV . 
    {
        ArrayList<CloudCoin> cloudCoins = new ArrayList<>();
        ArrayList<String> lines;
        try {
            lines = new ArrayList<>(Files.readAllLines(Paths.get(fileName)));
            //var lines = File.ReadAllLines(fileName).Select(a => a.split(","));
            for (String line : lines)
                cloudCoins.add(CloudCoin.FromCSV(line));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cloudCoins;
    }

    private CloudCoin importJPEG(String fileName)//Move one jpeg to suspect folder.
    {
        // boolean isSuccessful = false;
        // System.out.println("Trying to load: " + this.fileUtils.importFolder + fileName );
        System.out.println("Trying to load: " + ImportFolder + fileName);
        try {
            //  System.out.println("Loading coin: " + fileUtils.importFolder + fileName);
            //CloudCoin tempCoin = this.fileUtils.loadOneCloudCoinFromJPEGFile( fileUtils.importFolder + fileName );

            /*Begin import from jpeg*/

            /* GET the first 455 bytes of he jpeg where the coin is located */
            String wholeString = "";
            byte[] jpegHeader = new byte[455];
            // System.out.println("Load file path " + fileUtils.importFolder + fileName);

            int count;                            // actual number of bytes read
            int sum = 0;                          // total number of bytes read

            FileInputStream inputStream = new FileInputStream(new File(fileName));
            // read until Read method returns 0 (end of the stream has been reached)
            while ((count = inputStream.read(jpegHeader, sum, 455 - sum)) > 0) {
                sum += count;  // sum is a buffer offset for next reading
            }
            inputStream.close();

            wholeString = bytesToHexString(jpegHeader);
            CloudCoin tempCoin = parseJpeg(wholeString);
            // System.out.println("From FileUtils returnCC.fileName " + tempCoin.fileName);
            /*end import from jpeg file */
            //   System.out.println("Loaded coin filename: " + tempCoin.fileName);
            writeTo(SuspectFolderOld, tempCoin);
            return tempCoin;
        } catch (IOException e) {
            System.out.println("IO Exception:" + fileName + e);
            e.printStackTrace();
            //CoreLogger.Log("IO Exception:" + fileName + ioex);
        }
        return null;
    }

    public CloudCoin LoadCoin(String fileName) {
        CloudCoin[] coins = Utils.LoadJson(fileName);

        if (coins != null && coins.length > 0)
            return coins[0];
        return null;
    }


    // en d json test
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
        cc.CalcExpirationDate();
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
        RemoveCoins(coins, folder, ".stack");
    }
    public void RemoveCoins(ArrayList<CloudCoin> coins, String folder, String extension) {
        for (CloudCoin coin : coins) {
            try {
                System.out.println("deleting" + folder + coin.currentFilename + extension);
                Files.deleteIfExists(Paths.get(folder + coin.currentFilename + extension));
            } catch (IOException e) {
                System.out.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    public void WriteCoinsToFile(ArrayList<CloudCoin> coins, String fileName, String extension) {
        Gson gson = Utils.createGson();
        try {
            Stack stack = new Stack(coins.toArray(new CloudCoin[0]));
            Files.write(Paths.get(fileName + extension), gson.toJson(stack).getBytes());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void WriteCoinToFile(CloudCoin coin, String filename) {
        Stack stack = new Stack(coin);
        try {
            Files.write(Paths.get(filename), Utils.createGson().toJson(stack).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean writeQrCode(CloudCoin cc, String tag) {
        /* TODO TODO QRCODE
        String fileName = ExportFolder + cc.FileName() + "qr." + tag + ".jpg";
        cc.pan = null;
        QRCodeGenerator qrGenerator = new QRCodeGenerator();
        String json = JsonConvert.SerializeObject(cc);

        try
        {
            json.replace("\\", "");
            QRCodeData qrCodeData = qrGenerator.CreateQrCode(cc.GetCSV(), QRCodeGenerator.ECCLevel.Q);
            QRCode qrCode = new QRCode(qrCodeData);
            System.Drawing.Bitmap qrCodeImage = qrCode.GetGraphic(20);

            qrCodeImage.Save(fileName);

            return true;
        }
        catch(Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            return false;
        }*/
        return false;
    }

    public boolean writeBarCode() {
        /* TODO BARCODE
        String fileName = ExportFolder + cc.FileName + "barcode." + tag + ".jpg";
        cc.pan = null;
        QRCodeGenerator qrGenerator = new QRCodeGenerator();


        try
        {
            String json = JsonConvert.SerializeObject(cc);
            var barcode = new Barcode(json, Settings.Default);
            barcode.Canvas.SaveBmp(fileName);

            return true;
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            return false;
        }*/
        return true;
    }

    public abstract boolean WriteCoinToJpeg(CloudCoin cloudCoin, String TemplateFile, String OutputFile, String tag);

    public abstract boolean WriteCoinToQRCode(CloudCoin cloudCoin, String OutputFile);

    public abstract boolean WriteCoinToBARCode(CloudCoin cloudCoin, String OutputFile);

    public String GetCoinTemplate(CloudCoin cloudCoin) {
        int denomination = cloudCoin.denomination;
        String TemplatePath = "";
        switch (denomination) {
            case 1:
                TemplatePath = this.TemplateFolder + "jpeg1.jpg";
                break;
            case 5:
                TemplatePath = this.TemplateFolder + "jpeg5.jpg";
                break;
            case 25:
                TemplatePath = this.TemplateFolder + "jpeg25.jpg";
                break;
            case 100:
                TemplatePath = this.TemplateFolder + "jpeg100.jpg";
                break;
            case 250:
                TemplatePath = this.TemplateFolder + "jpeg250.jpg";
                break;

            default:
                break;

        }
        return TemplatePath;
    }

    public boolean writeJpeg(CloudCoin cc, String tag) {
        /* TODO JPEG
        // System.out.println("Writing jpeg " + cc.getSn());

        //  CoinUtils cu = new CoinUtils(cc);

        boolean fileSavedSuccessfully = true;

        // BUILD THE CLOUDCOIN String //
        String cloudCoinStr = "01C34A46494600010101006000601D05"; //THUMBNAIL HEADER BYTES
        for (int i = 0; (i < 25); i++)
        {
            cloudCoinStr = cloudCoinStr + cc.an.get(i);
        }

        //cloudCoinStr += "204f42455920474f4420262044454645415420545952414e545320";// Hex for " OBEY GOD & DEFEAT TYRANTS "
        //cloudCoinStr += "20466f756e6465727320372d352d3137";// Founders 7-5-17
        cloudCoinStr += "4c6976652046726565204f7220446965";// Live Free or Die
        cloudCoinStr += "00000000000000000000000000";//Set to unknown so program does not export user data
        // for (int i =0; i < 25; i++) {
        //     switch () { }//end switch pown char
        // }//end for each pown
        cloudCoinStr += "00"; // HC: Has comments. 00 = No
        cc.CalcExpirationDate();
        cloudCoinStr += cc.edHex; // 01;//Expiration date Sep 2016 (one month after zero month)
        cloudCoinStr += "01";//  cc.nn;//network number
        String hexSN = cc.getSn().ToString("X6");
        String fullHexSN = "";
        switch (hexSN.length())
        {
            case 1: fullHexSN = ("00000" + hexSN); break;
            case 2: fullHexSN = ("0000" + hexSN); break;
            case 3: fullHexSN = ("000" + hexSN); break;
            case 4: fullHexSN = ("00" + hexSN); break;
            case 5: fullHexSN = ("0" + hexSN); break;
            case 6: fullHexSN = hexSN; break;
        }
        cloudCoinStr = (cloudCoinStr + fullHexSN);
        // BYTES THAT WILL GO FROM 04 to 454 (Inclusive)//
        byte[] ccArray = this.hexStringToByteArray(cloudCoinStr);


        // READ JPEG TEMPLATE//
        byte[] jpegBytes = null;
        switch (cc.getDenomination())
        {
            case 1: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg1.jpg"); break;
            case 5: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg5.jpg"); break;
            case 25: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg25.jpg"); break;
            case 100: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg100.jpg"); break;
            case 250: jpegBytes = readAllBytes(this.TemplateFolder + "jpeg250.jpg"); break;
        }


        // WRITE THE SERIAL NUMBER ON THE JPEG //

        //Bitmap bitmapimage;
        SKBitmap bitmapimage;
        //using (var ms = new MemoryStream(jpegBytes))
        {

            //bitmapimage = new Bitmap(ms);
            bitmapimage = SKBitmap.Decode(jpegBytes);
        }
        SKCanvas canvas = new SKCanvas(bitmapimage);
        //Graphics graphics = Graphics.FromImage(bitmapimage);
        //graphics.SmoothingMode = SmoothingMode.AntiAlias;
        //graphics.InterpolationMode = InterpolationMode.HighQualityBicubic;
        SKPaint textPaint = new SKPaint()
        {
            IsAntialias = true,
            Color = SKColors.White,
            TextSize = 14,
            Typeface = SKTypeface.FromFamilyName("Arial")
        };
        //PointF drawPointAddress = new PointF(30.0F, 25.0F);

        canvas.DrawText(String.format("{0:N0}", cc.getSn()) + " of 16,777,216 on Network: 1", 30, 40, textPaint);
        //graphics.DrawString(String.format("{0:N0}", cc.getSn()) + " of 16,777,216 on Network: 1", new Font("Arial", 10), Brushes.White, drawPointAddress);

        //ImageConverter converter = new ImageConverter();
        //byte[] snBytes = (byte[])converter.ConvertTo(bitmapimage, typeof(byte[]));
        SKImage image = SKImage.FromBitmap(bitmapimage);
        SKData data = image.Encode(SKEncodedImageFormat.Jpeg, 100);
        byte[] snBytes = data.toArray();

        ArrayList<byte> b1 = new ArrayList<byte>(snBytes);
        ArrayList<byte> b2 = new ArrayList<byte>(ccArray);
        b1.InsertRange(4, b2);

        if (tag == "random")
        {
            Random r = new Random();
            int rInt = r.Next(100000, 1000000); //for ints
            tag = rInt.toString();
        }

        String fileName = ExportFolder + cc.FileName() + tag + ".jpg";
        File.WriteAllBytes(fileName, b1.toArray());
        System.out.println("Writing to " + fileName);
        //CoreLogger.Log("Writing to " + fileName);
        return fileSavedSuccessfully;*/
        return false;
    }//end write JPEG


    public String bytesToHexString(byte[] data) {
        int length = data.length;
        char[] hex = new char[length * 2];
        int num1 = 0;
        for (int index = 0; index < length * 2; index += 2) {
            byte num2 = data[num1++];
            hex[index] = GetHexValue(num2 / 0x10);
            hex[index + 1] = GetHexValue(num2 % 0x10);
        }
        return new String(hex);
    }//End NewConverted//

    private char GetHexValue(int i) {
        if (i < 10) {
            return (char) (i + 0x30);
        }
        return (char) ((i - 10) + 0x41);
    }//end GetHexValue

    /* Writes a JPEG To the Export Folder */

    /* OPEN FILE AND READ ALL CONTENTS AS BYTE ARRAY */
    public byte[] readAllBytes(String fileName) {
        try {
            return Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }//end read all bytes

    public boolean writeTo(String folder, CloudCoin cc) {
        //CoinUtils cu = new CoinUtils(cc);
        final String quote = "\"";
        final String tab = "\t";
        String wholeJson = "{" + System.lineSeparator(); //{
        boolean alreadyExists = true;
        String json = this.setJSON(cc);

        try {
            if (!Files.exists(Paths.get(folder + cc.FileName() + ".stack"))) {
                wholeJson += tab + quote + "cloudcoin" + quote + ": [" + System.lineSeparator(); // "cloudcoin" : [
                wholeJson += json;
                wholeJson += System.lineSeparator() + tab + "]" + System.lineSeparator() + "}";
                alreadyExists = true;
                Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            } else {
                if (folder.contains("Counterfeit") || folder.contains("Trash")) {
                    //Let the program delete it
                    alreadyExists = false;
                    return alreadyExists;
                } else if (folder.contains("Imported")) {
                    alreadyExists = false;
                    Files.delete(Paths.get(folder + cc.FileName() + ".stack"));
                    Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
                    return alreadyExists;
                } else {
                    System.out.println(cc.FileName() + ".stack" + " already exists in the folder " + folder);
                    //CoreLogger.Log(cu.fileName + ".stack" + " already exists in the folder " + folder);
                    return alreadyExists;
                }//end else
            }//File Exists
            // TODO: Should not write text twice
            Files.write(Paths.get(folder + cc.FileName() + ".stack"), wholeJson.getBytes(StandardCharsets.UTF_8));
            alreadyExists = false;
            return alreadyExists;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }//End Write To

    public CloudCoin parseJpeg(String wholeString) {

        CloudCoin cc = new CloudCoin();
        int startAn = 40;
        for (int i = 0; i < 25; i++) {
            cc.an.add(wholeString.substring(startAn, 32));
            //ccan.set(i, wholeString.substring(startAn, 32));
            // System.out.println(i +": " + cc.an[i]);
            startAn += 32;
        }


        cc.aoid = null;
        // wholeString.substring( 840, 895 );
        //cc.hp = 25;
        // Integer.parseInt(wholeString.substring( 896, 896 ), 16);
        cc.ed = wholeString.substring(898, 4);

        cc.nn = Integer.valueOf(wholeString.substring(902, 2), 16);
        cc.setSn(Integer.valueOf(wholeString.substring(904, 6), 16));
        cc.pown = "uuuuuuuuuuuuuuuuuuuuuuuuu";
        //  System.out.println("parseJpeg cc.fileName " + cc.fileName);
        return cc;
    }

    // en d json test
    public byte[] hexStringToByteArray(String HexString) {
        int NumberChars = HexString.length();
        byte[] bytes = new byte[NumberChars / 2];
        for (int i = 0; i < NumberChars; i += 2) {
            bytes[i / 2] = Byte.valueOf(HexString.substring(i, 2), 16);
        }
        return bytes;
    }//End hex String to byte array
}
