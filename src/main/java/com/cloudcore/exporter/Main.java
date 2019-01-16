package com.cloudcore.exporter;

import com.cloudcore.exporter.core.FileSystem;
import com.cloudcore.exporter.desktop.FolderWatcher;
import com.cloudcore.exporter.server.Command;
import com.cloudcore.exporter.utils.SimpleLogger;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        SimpleLogger.writeLog("ServantExporterStarted", "");
        ArrayList<Command> commands;

        while (true) {
            try {
                if (args.length != 0 && Files.exists(Paths.get(args[0]))) {
                    System.out.println("New root path: " + args[0]);
                    FileSystem.changeRootPath(args[0]);
                }
                FileSystem.createDirectories();

                FolderWatcher watcher = new FolderWatcher(FileSystem.CommandsFolder);
                System.out.println("Watching for commands at " + FileSystem.CommandsFolder);
                boolean stop = false;

                commands = FileSystem.getCommands();
                if (commands.size() > 0)
                    for (Command command : commands) {
                        boolean successful = Exporter.ExportCoins(Exporter.calculateNotesForTotal("", command.amount),
                                command.type, "", command.tag);
                        FileSystem.archiveCommand(command);
                    }

                while (!stop) {
                    if (watcher.newFileDetected()) {
                        System.out.println(Instant.now().toString() + ": Exporting coins...");
                        commands = FileSystem.getCommands();
                        if (commands.size() > 0)
                            for (Command command : commands) {
                                boolean successful = Exporter.ExportCoins(Exporter.calculateNotesForTotal("", command.amount),
                                        command.type, "", command.tag);
                                FileSystem.archiveCommand(command);
                            }
                    }
                }
            } catch (Exception e) {
                System.out.println("Uncaught exception - " + e.getLocalizedMessage());
            }
        }
    }
}
