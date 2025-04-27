package com.qserver.loganalyzer.parser;

import com.qserver.loganalyzer.model.Transaction;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for QServer transaction log files.
 * This class is responsible for reading and parsing CSV log files into Transaction objects.
 */
public class TransactionLogParser {
    private static final Logger logger = LoggerFactory.getLogger(TransactionLogParser.class);
    
    // CSV column headers based on QServer transaction log format
    private static final String COL_ID = "id";
    private static final String COL_KIND = "kind";
    private static final String COL_THREAD = "thread";
    private static final String COL_START_TIME = "starttime";
    private static final String COL_END_TIME = "endtime";
    private static final String COL_WAIT_TIME = "waittime";
    private static final String COL_PROCESSING_TIME = "proctime";
    private static final String COL_BEGIN_TIME = "begintime";
    private static final String COL_FUNC_TIME = "functime";
    private static final String COL_DB_TIME = "dbtime";
    private static final String COL_MEMORY_COMMIT_TIME = "mctime";
    private static final String COL_STREAM_TIME = "streamtime";
    private static final String COL_KERNEL_TIME = "kerneltime";
    private static final String COL_CLEANUP_TIME = "cleanuptime";
    private static final String COL_END_PROCESS_TIME = "endtime";
    private static final String COL_STATUS = "status";
    private static final String COL_INITIATOR = "initiator";
    private static final String COL_DETAILS = "details";
    
    // Date-time formatter for parsing timestamps
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Parses a QServer transaction log file and returns a list of Transaction objects.
     *
     * @param filePath Path to the CSV log file
     * @return List of Transaction objects
     * @throws IOException If an I/O error occurs
     */
    public List<Transaction> parseTransactionLog(String filePath) throws IOException {
        logger.info("Parsing transaction log file: {}", filePath);
        List<Transaction> transactions = new ArrayList<>();
        
        try (Reader reader = new FileReader(filePath);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            for (CSVRecord record : csvParser) {
                try {
                    Transaction transaction = parseRecord(record);
                    transactions.add(transaction);
                } catch (Exception e) {
                    logger.warn("Error parsing record: {}", e.getMessage());
                }
            }
        }
        
        logger.info("Successfully parsed {} transactions from {}", transactions.size(), filePath);
        return transactions;
    }
    
    /**
     * Parses a CSV record into a Transaction object.
     *
     * @param record CSV record to parse
     * @return Transaction object
     */
    private Transaction parseRecord(CSVRecord record) {
        Transaction transaction = new Transaction();
        
        // Parse required fields
        transaction.setId(parseLong(record, COL_ID));
        transaction.setKind(record.get(COL_KIND));
        transaction.setThread(record.get(COL_THREAD));
        transaction.setStartTime(parseDateTime(record, COL_START_TIME));
        transaction.setEndTime(parseDateTime(record, COL_END_TIME));
        transaction.setWaitTime(parseDouble(record, COL_WAIT_TIME));
        transaction.setProcessingTime(parseDouble(record, COL_PROCESSING_TIME));
        
        // Parse optional fields
        if (record.isMapped(COL_BEGIN_TIME)) {
            transaction.setBeginTime(parseDouble(record, COL_BEGIN_TIME));
        }
        if (record.isMapped(COL_FUNC_TIME)) {
            transaction.setFuncTime(parseDouble(record, COL_FUNC_TIME));
        }
        if (record.isMapped(COL_DB_TIME)) {
            transaction.setDbTime(parseDouble(record, COL_DB_TIME));
        }
        if (record.isMapped(COL_MEMORY_COMMIT_TIME)) {
            transaction.setMemoryCommitTime(parseDouble(record, COL_MEMORY_COMMIT_TIME));
        }
        if (record.isMapped(COL_STREAM_TIME)) {
            transaction.setStreamTime(parseDouble(record, COL_STREAM_TIME));
        }
        if (record.isMapped(COL_KERNEL_TIME)) {
            transaction.setKernelTime(parseDouble(record, COL_KERNEL_TIME));
        }
        if (record.isMapped(COL_CLEANUP_TIME)) {
            transaction.setCleanupTime(parseDouble(record, COL_CLEANUP_TIME));
        }
        if (record.isMapped(COL_END_PROCESS_TIME)) {
            transaction.setEndProcessTime(parseDouble(record, COL_END_PROCESS_TIME));
        }
        if (record.isMapped(COL_STATUS)) {
            transaction.setStatus(record.get(COL_STATUS));
        }
        if (record.isMapped(COL_INITIATOR)) {
            transaction.setInitiator(record.get(COL_INITIATOR));
        }
        if (record.isMapped(COL_DETAILS)) {
            transaction.setDetails(record.get(COL_DETAILS));
        }
        
        return transaction;
    }
    
    /**
     * Parses a string value from a CSV record into a long.
     *
     * @param record CSV record
     * @param columnName Column name
     * @return Parsed long value, or 0 if parsing fails
     */
    private long parseLong(CSVRecord record, String columnName) {
        try {
            return Long.parseLong(record.get(columnName));
        } catch (NumberFormatException e) {
            logger.warn("Error parsing long value for column {}: {}", columnName, e.getMessage());
            return 0;
        }
    }
    
    /**
     * Parses a string value from a CSV record into a double.
     *
     * @param record CSV record
     * @param columnName Column name
     * @return Parsed double value, or 0.0 if parsing fails
     */
    private double parseDouble(CSVRecord record, String columnName) {
        try {
            return Double.parseDouble(record.get(columnName));
        } catch (NumberFormatException e) {
            logger.warn("Error parsing double value for column {}: {}", columnName, e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Parses a string value from a CSV record into a LocalDateTime.
     *
     * @param record CSV record
     * @param columnName Column name
     * @return Parsed LocalDateTime, or null if parsing fails
     */
    private LocalDateTime parseDateTime(CSVRecord record, String columnName) {
        try {
            String dateTimeStr = record.get(columnName);
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            logger.warn("Error parsing date-time value for column {}: {}", columnName, e.getMessage());
            return null;
        }
    }
}
