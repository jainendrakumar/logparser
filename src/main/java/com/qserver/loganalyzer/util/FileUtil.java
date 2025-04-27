package com.qserver.loganalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for file operations.
 * This class provides methods for file handling and path management.
 */
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    
    /**
     * Creates a directory if it doesn't exist.
     *
     * @param dirPath Path to the directory
     * @return true if directory exists or was created successfully, false otherwise
     */
    public static boolean createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                logger.warn("Path exists but is not a directory: {}", dirPath);
                return false;
            }
            return true;
        }
        
        boolean created = dir.mkdirs();
        if (created) {
            logger.info("Created directory: {}", dirPath);
        } else {
            logger.error("Failed to create directory: {}", dirPath);
        }
        
        return created;
    }
    
    /**
     * Generates a timestamped file name.
     *
     * @param baseName Base file name
     * @param extension File extension (without dot)
     * @return Timestamped file name
     */
    public static String generateTimestampedFileName(String baseName, String extension) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        
        return baseName + "_" + timestamp + "." + extension;
    }
    
    /**
     * Checks if a file exists.
     *
     * @param filePath Path to the file
     * @return true if file exists, false otherwise
     */
    public static boolean fileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }
    
    /**
     * Gets the file extension from a file path.
     *
     * @param filePath Path to the file
     * @return File extension (without dot), or empty string if no extension
     */
    public static String getFileExtension(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            return "";
        }
        
        return filePath.substring(lastDotIndex + 1);
    }
    
    /**
     * Gets the file name without extension from a file path.
     *
     * @param filePath Path to the file
     * @return File name without extension
     */
    public static String getFileNameWithoutExtension(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        
        return fileName.substring(0, lastDotIndex);
    }
    
    /**
     * Combines path components into a single path.
     *
     * @param basePath Base path
     * @param components Additional path components
     * @return Combined path
     */
    public static String combinePath(String basePath, String... components) {
        StringBuilder path = new StringBuilder(basePath);
        
        for (String component : components) {
            if (!path.toString().endsWith(File.separator) && !component.startsWith(File.separator)) {
                path.append(File.separator);
            } else if (path.toString().endsWith(File.separator) && component.startsWith(File.separator)) {
                component = component.substring(1);
            }
            
            path.append(component);
        }
        
        return path.toString();
    }
}
