package com.qserver.loganalyzer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility class for configuration management.
 * This class provides methods to load and access configuration properties.
 */
public class ConfigurationManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
    
    private Properties properties;
    
    /**
     * Default constructor that initializes with default properties.
     */
    public ConfigurationManager() {
        properties = new Properties();
        setDefaults();
    }
    
    /**
     * Constructor that loads properties from a file.
     *
     * @param configFile Path to the configuration file
     * @throws IOException If an I/O error occurs
     */
    public ConfigurationManager(String configFile) throws IOException {
        properties = new Properties();
        setDefaults();
        loadFromFile(configFile);
    }
    
    /**
     * Sets default configuration values.
     */
    private void setDefaults() {
        properties.setProperty("topTransactions", "10");
        properties.setProperty("topKinds", "5");
        properties.setProperty("topThreads", "5");
        properties.setProperty("chartWidth", "800");
        properties.setProperty("chartHeight", "600");
        properties.setProperty("reportFormat", "docx,xlsx");
    }
    
    /**
     * Loads configuration from a properties file.
     *
     * @param configFile Path to the configuration file
     * @throws IOException If an I/O error occurs
     */
    public void loadFromFile(String configFile) throws IOException {
        File file = new File(configFile);
        if (!file.exists()) {
            logger.warn("Configuration file not found: {}", configFile);
            return;
        }
        
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
            logger.info("Loaded configuration from file: {}", configFile);
        }
    }
    
    /**
     * Gets a string property value.
     *
     * @param key Property key
     * @return Property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a string property value with a default.
     *
     * @param key Property key
     * @param defaultValue Default value to return if property is not found
     * @return Property value, or defaultValue if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Gets an integer property value.
     *
     * @param key Property key
     * @param defaultValue Default value to return if property is not found or invalid
     * @return Property value as integer, or defaultValue if not found or invalid
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer property {}: {}", key, value);
            return defaultValue;
        }
    }
    
    /**
     * Gets a boolean property value.
     *
     * @param key Property key
     * @param defaultValue Default value to return if property is not found
     * @return Property value as boolean, or defaultValue if not found
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Sets a property value.
     *
     * @param key Property key
     * @param value Property value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
}
