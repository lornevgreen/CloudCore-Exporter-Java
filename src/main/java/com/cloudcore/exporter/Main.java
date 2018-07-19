package com.cloudcore.exporter;

import com.cloudcore.exporter.core.FileSystem;
import com.cloudcore.exporter.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static String rootFolder = Paths.get("C:/CloudCoins-Exporter").toAbsolutePath().toString();

    static FileSystem FS;
    public static SimpleLogger logger;

    public static void main(String[] args) {
        try {
            setup();
            Exporter exporter = new Exporter(FS);
            exporter.logger = logger;

            System.out.println("Exporting Coins...");
            exporter.ExportCoins();
        } catch (Exception e) {
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private static void setup() {
        FS = new FileSystem(rootFolder);
        FS.CreateDirectories();
        FS.LoadFileSystem();

        logger = new SimpleLogger(FS.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log", true);
    }
}
