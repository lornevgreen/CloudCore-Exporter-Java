package com.cloudcore.exporter;

import com.cloudcore.exporter.core.FileSystem;
import com.cloudcore.exporter.utils.SimpleLogger;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {


    /* Constants */

    public static final String rootFolder = Paths.get("C:/CloudCoins-Exporter").toAbsolutePath().toString();


    /* Fields */

    static FileSystem fs;
    public static SimpleLogger logger;

    /* Methods */

    /**
     * Creates an Exporter instance and runs it.
     */
    public static void main(String[] args) {
        try {
            setup();
            Exporter exporter = new Exporter(fs);
            exporter.logger = logger;

            exporter.ExportCoins();
        } catch (Exception e) {
            System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            logger.appendLog(e.toString(), e.getStackTrace());
            e.printStackTrace();
        }

        logger.writeLogToFile();
    }

    /**
     * Sets up the FileSystem instance in the defined rootFolder.
     */
    private static void setup() {
        fs = new FileSystem(rootFolder);
        fs.createDirectories();
        fs.loadFileSystem();

        logger = new SimpleLogger(fs.LogsFolder + "logs" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")).toLowerCase() + ".log");
    }
}
