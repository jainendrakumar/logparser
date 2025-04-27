package com.qserver.loganalyzer.parser;

import com.qserver.loganalyzer.model.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for QServer transaction log files in CSV format.
 * This class is responsible for reading and parsing transaction log files.
 */
public class TransactionLogParser {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLogParser.class);
    
    // Regular expression to extract timestamp from filename
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("(\\d{8})_(\\d{4})");
    
    // Date formatter for parsing time strings
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ssXXX");
    
    /**
     * Parses a single transaction log file and returns a list of transactions.
     *
     * @param file the transaction log file to parse
     * @return a list of parsed transactions
     * @throws IOException if an I/O error occurs
     */
    public List<Transaction> parseFile(File file) throws IOException {
        logger.info("Parsing transaction log file: {}", file.getName());
        
        List<Transaction> transactions = new ArrayList<>();
        
        try (Reader reader = new FileReader(file);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                try {
                    Transaction transaction = parseRecord(record);
                    transaction.setSourceFile(file.getName());
                    
                    // Extract date from filename if possible
                    LocalDate fileDate = extractDateFromFilename(file.getName());
                    if (fileDate != null && transaction.getStartTime() != null) {
                        // Combine date from filename with time from record
                        LocalTime time = transaction.getStartTime().toLocalTime();
                        transaction.setStartTime(LocalDateTime.of(fileDate, time));
                    }
                    
                    transactions.add(transaction);
                } catch (Exception e) {
                    logger.warn("Error parsing record: {}", e.getMessage());
                }
            }
        }
        
        logger.info("Parsed {} transactions from file: {}", transactions.size(), file.getName());
        return transactions;
    }
    
    /**
     * Parses multiple transaction log files and returns a combined list of transactions.
     *
     * @param files the transaction log files to parse
     * @return a list of parsed transactions from all files
     */
    public List<Transaction> parseFiles(List<File> files) {
        List<Transaction> allTransactions = new ArrayList<>();
        
        for (File file : files) {
            try {
                List<Transaction> fileTransactions = parseFile(file);
                allTransactions.addAll(fileTransactions);
            } catch (IOException e) {
                logger.error("Error parsing file {}: {}", file.getName(), e.getMessage());
            }
        }
        
        logger.info("Parsed a total of {} transactions from {} files", allTransactions.size(), files.size());
        return allTransactions;
    }
    
    /**
     * Parses a CSV record into a Transaction object.
     *
     * @param record the CSV record to parse
     * @return the parsed Transaction object
     */
    private Transaction parseRecord(CSVRecord record) {
        Transaction transaction = new Transaction();
        
        // Parse basic transaction information
        transaction.setTransactionId(record.get("transactionid"));
        transaction.setTransactionKind(record.get("transactionkind"));
        transaction.setStatus(record.get("status"));
        
        // Parse thread and action information if available
        if (record.isMapped("threadname")) {
            transaction.setThreadName(record.get("threadname"));
        }
        if (record.isMapped("actionelementname")) {
            transaction.setActionElementName(record.get("actionelementname"));
        }
        
        // Parse time components
        if (record.isMapped("length")) {
            transaction.setLength(parseLongOrDefault(record.get("length"), 0));
        }
        if (record.isMapped("waitingtime")) {
            transaction.setWaitingTime(parseLongOrDefault(record.get("waitingtime"), 0));
        }
        if (record.isMapped("proctime")) {
            transaction.setProcTime(parseLongOrDefault(record.get("proctime"), 0));
        }
        if (record.isMapped("functime")) {
            transaction.setFuncTime(parseLongOrDefault(record.get("functime"), 0));
        }
        if (record.isMapped("dbtime")) {
            transaction.setDbTime(parseLongOrDefault(record.get("dbtime"), 0));
        }
        if (record.isMapped("streamtime")) {
            transaction.setStreamTime(parseLongOrDefault(record.get("streamtime"), 0));
        }
        
        // Parse memory components
        if (record.isMapped("procmem")) {
            transaction.setProcMem(parseLongOrDefault(record.get("procmem"), 0));
        }
        if (record.isMapped("funcmem")) {
            transaction.setFuncMem(parseLongOrDefault(record.get("funcmem"), 0));
        }
        if (record.isMapped("dbmem")) {
            transaction.setDbMem(parseLongOrDefault(record.get("dbmem"), 0));
        }
        if (record.isMapped("streammem")) {
            transaction.setStreamMem(parseLongOrDefault(record.get("streammem"), 0));
        }
        if (record.isMapped("osvmsize")) {
            transaction.setOsVmSize(parseLongOrDefault(record.get("osvmsize"), 0));
        }
        if (record.isMapped("freememory")) {
            transaction.setFreeMemory(parseLongOrDefault(record.get("freememory"), 0));
        }
        
        // Parse start time
        if (record.isMapped("starttime")) {
            String startTimeStr = record.get("starttime");
            try {
                // Parse time with timezone offset (e.g., "16:58:35+05:30")
                LocalTime time = LocalTime.parse(startTimeStr, TIME_FORMATTER);
                transaction.setStartTime(LocalDateTime.of(LocalDate.now(), time));
            } catch (DateTimeParseException e) {
                logger.warn("Error parsing start time: {}", startTimeStr);
            }
        }
        
        return transaction;
    }
    
    /**
     * Extracts the date from a filename using a regular expression pattern.
     *
     * @param filename the filename to extract the date from
     * @return the extracted date, or null if not found
     */
    private LocalDate extractDateFromFilename(String filename) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(filename);
        if (matcher.find()) {
            try {
                String dateStr = matcher.group(1);
                int year = Integer.parseInt(dateStr.substring(0, 4));
                int month = Integer.parseInt(dateStr.substring(4, 6));
                int day = Integer.parseInt(dateStr.substring(6, 8));
                return LocalDate.of(year, month, day);
            } catch (Exception e) {
                logger.warn("Error extracting date from filename: {}", filename);
            }
        }
        return null;
    }
    
    /**
     * Parses a string to a long value, returning a default value if parsing fails.
     *
     * @param value the string to parse
     * @param defaultValue the default value to return if parsing fails
     * @return the parsed long value or the default value
     */
    private long parseLongOrDefault(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
