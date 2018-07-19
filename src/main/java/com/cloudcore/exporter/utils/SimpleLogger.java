package com.cloudcore.exporter.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cloudcore.exporter.utils.SimpleLogger.LogLevel.*;

public class SimpleLogger {

    private DateTimeFormatter DatetimeFormat;
    private String Filename;

    /// <summary>
    /// Initialize a new instance of SimpleLogger class.
    /// Log file will be created automatically if not yet exists, else it can be either a fresh new file or append to the existing file.
    /// Default is create a fresh new log file.
    /// </summary>
    /// <param name="append">True to append to existing log file, False to overwrite and create new log file</param>


    public SimpleLogger(String FileName, boolean append) {
        initialize(FileName, append);
    }

    private void initialize(String FileName, boolean append) {
        DatetimeFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        this.Filename = FileName;

        String logHeader = Filename + " is created.";
        if (!Files.exists(Paths.get(Filename))) {
            WriteFormattedLog(INFO, logHeader);
        } else {
            if (!append)
                WriteFormattedLog(INFO, logHeader);
        }
    }


    /// <summary>
    /// Log an info message
    /// </summary>
    /// <param name="text">Message</param>
    public void Info(String text) {
        WriteFormattedLog(INFO, text);
    }

    /// <summary>
    /// Format a log message based on log level
    /// </summary>
    /// <param name="level">Log level</param>
    /// <param name="text">Log message</param>
    private void WriteFormattedLog(LogLevel level, String text) {
        String pretext;
        switch (level) {
            case TRACE:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [TRACE]   ";
                break;
            case INFO:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [INFO]    ";
                break;
            case DEBUG:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [DEBUG]   ";
                break;
            case WARNING:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [WARNING] ";
                break;
            case ERROR:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [ERROR]   ";
                break;
            case FATAL:
                pretext = LocalDateTime.now().format(DatetimeFormat) + " [FATAL]   ";
                break;
            default:
                pretext = "";
                break;
        }

        WriteLine(pretext + text);
    }

    /// <summary>
    /// Write a line of formatted log message into a log file
    /// </summary>
    /// <param name="text">Formatted log message</param>
    /// <param name="append">True to append, False to overwrite the file</param>
    /// <exception cref="System.IO.IOException"></exception>
    private void WriteLine(String text) {
        try {
            boolean append = false;
            StandardOpenOption option = (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

            Path path = Paths.get(Filename);
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Files.write(path, text.getBytes(StandardCharsets.UTF_8), option);
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    /// <summary>
    /// Supported log level
    /// </summary>
    //[Flags]
    enum LogLevel {
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FATAL
    }
}
