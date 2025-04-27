package com.qserver.loganalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for logging and error handling.
 * This class provides methods for consistent logging and error handling across the application.
 */
public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Gets the stack trace of an exception as a string.
     *
     * @param throwable The exception to get the stack trace from
     * @return Stack trace as a string
     */
    public static String getStackTraceAsString(Throwable throwable) {
        if (throwable == null) {
            return "";
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Logs the start of a process.
     *
     * @param processName Name of the process
     */
    public static void logProcessStart(String processName) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        logger.info("=== Process Started: {} at {} ===", processName, timestamp);
    }
    
    /**
     * Logs the end of a process.
     *
     * @param processName Name of the process
     */
    public static void logProcessEnd(String processName) {
        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        logger.info("=== Process Completed: {} at {} ===", processName, timestamp);
    }
    
    /**
     * Logs an error with detailed information.
     *
     * @param message Error message
     * @param throwable Exception that occurred
     */
    public static void logError(String message, Throwable throwable) {
        logger.error(message);
        logger.error("Error details: {}", getStackTraceAsString(throwable));
    }
}
