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


    /* Fields */

    private DateTimeFormatter logTimestampFormat = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private StringBuilder logsRecord;

    private String fullFilePath;


    /* Constructor */

    /**
     * Initialize a new instance of SimpleLogger class.
     *
     * @param fullFilePath the absolute filepath of the log.
     */
    public SimpleLogger(String fullFilePath) {
        initialize(fullFilePath);
    }


    /* Methods */

    /**
     * Initializes a new master log and its filepath.
     *
     * @param fullFilePath the absolute filepath of the log.
     */
    private void initialize(String fullFilePath) {
        this.fullFilePath = fullFilePath;
        logsRecord = new StringBuilder();
    }

    /**
     * Log a message to the master log.
     *
     * @param text the text to append to the log.
     */
    public void appendLog(String text) {
        logsRecord.append(text).append(System.lineSeparator());
    }

    /**
     * Log an error message to the master log.
     *
     * @param text               the text to append to the log.
     * @param stackTraceElements the stack trace of an exception.
     */
    public void appendLog(String text, StackTraceElement[] stackTraceElements) {
        logsRecord.append(text).append(System.lineSeparator());
        for (StackTraceElement stack : stackTraceElements)
            logsRecord.append("    at ").append(stack.toString()).append(System.lineSeparator());
    }

    /**
     * Writes all log messages to a file.
     */
    public void writeLogToFile() {
        writeFormattedLog(INFO, logsRecord.toString());
    }

    /// <summary>
    /// Format a log message based on log level
    /// </summary>
    /// <param name="level">Log level</param>
    /// <param name="text">Log message</param>
    private void writeFormattedLog(LogLevel level, String text) {
        String pretext;
        switch (level) {
            case TRACE:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [TRACE]   ";
                break;
            case INFO:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [INFO]    ";
                break;
            case DEBUG:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [DEBUG]   ";
                break;
            case WARNING:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [WARNING] ";
                break;
            case ERROR:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [ERROR]   ";
                break;
            case FATAL:
                pretext = LocalDateTime.now().format(logTimestampFormat) + " [FATAL]   ";
                break;
            default:
                pretext = "";
                break;
        }

        writeLine(pretext + text);
    }

    /**
     * Write a formatted log message into a log file.
     *
     * @param text formatted log message.
     */
    private void writeLine(String text) {
        writeLine(text, false);
    }

    /**
     * Write a formatted log message into a log file.
     *
     * @param text   formatted log message.
     * @param append true to append to an existing file, false to overwrite the file.
     */
    private void writeLine(String text, boolean append) {
        try {
            StandardOpenOption option = (append) ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING;

            Path path = Paths.get(fullFilePath);
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

    /**
     * Supported log levels.
     */
    enum LogLevel {
        TRACE,
        INFO,
        DEBUG,
        WARNING,
        ERROR,
        FATAL
    }
}
