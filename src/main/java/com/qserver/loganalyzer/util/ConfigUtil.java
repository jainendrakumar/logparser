package com.qserver.loganalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility class for application configuration.
 * This class provides methods to create and save default configuration.
 */
public class ConfigUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    
    /**
     * Creates a default configuration file if it doesn't exist.
     *
     * @param configFilePath Path where the configuration file should be created
     * @return true if file was created or already exists, false if creation failed
     */
    public static boolean createDefaultConfigFile(String configFilePath) {
        File configFile = new File(configFilePath);
        if (configFile.exists()) {
            logger.info("Configuration file already exists: {}", configFilePath);
            return true;
        }
        
        Properties defaultProperties = new Properties();
        defaultProperties.setProperty("topTransactions", "10");
        defaultProperties.setProperty("topKinds", "5");
        defaultProperties.setProperty("topThreads", "5");
        defaultProperties.setProperty("chartWidth", "800");
        defaultProperties.setProperty("chartHeight", "600");
        defaultProperties.setProperty("reportFormat", "docx,xlsx");
        
        try {
            // Ensure parent directory exists
            Path parentDir = Paths.get(configFile.getParent());
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            
            // Save properties to file
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                defaultProperties.store(out, "QServer Transaction Log Analyzer Configuration");
                logger.info("Created default configuration file: {}", configFilePath);
                return true;
            }
        } catch (IOException e) {
            logger.error("Failed to create default configuration file: {}", e.getMessage(), e);
            return false;
        }
    }
}
